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
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosAPI;

import java.util.function.Supplier;

public class SPacketSyncActive {

  private int     entityId;
  private String  curioId;
  private boolean remove;

  public SPacketSyncActive(int entityId, String curioId, boolean remove) {

    this.entityId = entityId;
    this.curioId = curioId;
    this.remove = remove;
  }

  public static void encode(SPacketSyncActive msg, PacketBuffer buf) {

    buf.writeInt(msg.entityId);
    buf.writeString(msg.curioId);
    buf.writeBoolean(msg.remove);
  }

  public static SPacketSyncActive decode(PacketBuffer buf) {

    return new SPacketSyncActive(buf.readInt(), buf.readString(25), buf.readBoolean());
  }

  public static void handle(SPacketSyncActive msg, Supplier<NetworkEvent.Context> ctx) {

    ctx.get().enqueueWork(() -> {
      Entity entity = Minecraft.getInstance().world.getEntityByID(msg.entityId);

      if (entity instanceof LivingEntity) {
        CuriosAPI.getCuriosHandler((LivingEntity) entity).ifPresent(handler -> {

          if (msg.remove) {
            handler.disableCurio(msg.curioId);
          } else {
            handler.enableCurio(msg.curioId);
          }
        });
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
