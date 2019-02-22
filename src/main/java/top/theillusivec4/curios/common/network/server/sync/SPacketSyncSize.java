/*
 * Copyright (C) 2018-2019  C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.curios.common.network.server.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosAPI;

import java.util.function.Supplier;

public class SPacketSyncSize {

    private int entityId;
    private String curioId;
    private int amount;
    private boolean remove;

    public SPacketSyncSize(int entityId, String curioId, int amount, boolean remove) {
        this.entityId = entityId;
        this.curioId = curioId;
        this.amount = amount;
        this.remove = remove;
    }

    public static void encode(SPacketSyncSize msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeString(msg.curioId);
        buf.writeInt(msg.amount);
        buf.writeBoolean(msg.remove);
    }

    public static SPacketSyncSize decode(PacketBuffer buf) {
        return new SPacketSyncSize(buf.readInt(), buf.readString(25), buf.readInt(), buf.readBoolean());
    }

    public static void handle(SPacketSyncSize msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().world.getEntityByID(msg.entityId);

            if (entity instanceof EntityLivingBase) {
                CuriosAPI.getCuriosHandler((EntityLivingBase) entity).ifPresent(handler -> {

                    if (msg.remove) {
                        handler.removeCurioSlot(msg.curioId, msg.amount);
                    } else {
                        handler.addCurioSlot(msg.curioId, msg.amount);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
