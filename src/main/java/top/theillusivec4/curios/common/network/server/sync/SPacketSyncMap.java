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

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;

import java.util.Map;
import java.util.SortedMap;
import java.util.function.Supplier;

public class SPacketSyncMap {

    private int entityId;
    private int entrySize;
    private SortedMap<String, CurioStackHandler> map;

    public SPacketSyncMap(int entityId, SortedMap<String, CurioStackHandler> map) {
        this.entityId = entityId;
        this.entrySize = map.size();
        this.map = map;
    }

    public static void encode(SPacketSyncMap msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeInt(msg.entrySize);

        for (Map.Entry<String, CurioStackHandler> entry : msg.map.entrySet()) {
            buf.writeString(entry.getKey());
            buf.writeCompoundTag(entry.getValue().serializeNBT());
        }
    }

    public static SPacketSyncMap decode(PacketBuffer buf) {
        int entityId = buf.readInt();
        int entrySize = buf.readInt();
        SortedMap<String, CurioStackHandler> map = Maps.newTreeMap();

        for (int i = 0; i < entrySize; i++) {
            String key = buf.readString(25);
            CurioStackHandler stackHandler = new CurioStackHandler();
            CompoundNBT compound = buf.readCompoundTag();

            if (compound != null) {
                stackHandler.deserializeNBT(compound);
            }
            map.put(key, stackHandler);
        }
        return new SPacketSyncMap(entityId, map);
    }

    public static void handle(SPacketSyncMap msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().world.getEntityByID(msg.entityId);

            if (entity instanceof LivingEntity) {
                CuriosAPI.getCuriosHandler((LivingEntity) entity).ifPresent(handler -> handler.setCurioMap(msg.map));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
