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
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosAPI;

import java.util.function.Supplier;

public class SPacketSyncContents {

  private int       entityId;
  private int       slotId;
  private String    curioId;
  private ItemStack stack;

  public SPacketSyncContents(int entityId, String curioId, int slotId, ItemStack stack) {

    this.entityId = entityId;
    this.slotId = slotId;
    this.stack = stack.copy();
    this.curioId = curioId;
  }

  public static void encode(SPacketSyncContents msg, PacketBuffer buf) {

    buf.writeInt(msg.entityId);
    buf.writeString(msg.curioId);
    buf.writeInt(msg.slotId);
    buf.writeItemStack(msg.stack);
  }

  public static SPacketSyncContents decode(PacketBuffer buf) {

    return new SPacketSyncContents(buf.readInt(), buf.readString(25), buf.readInt(),
                                   buf.readItemStack());
  }

  public static void handle(SPacketSyncContents msg, Supplier<NetworkEvent.Context> ctx) {

    ctx.get().enqueueWork(() -> {
      Entity entity = Minecraft.getInstance().world.getEntityByID(msg.entityId);

      if (entity instanceof LivingEntity) {
        CuriosAPI.getCuriosHandler((LivingEntity) entity)
                 .ifPresent(handler -> handler.setStackInSlot(msg.curioId, msg.slotId, msg.stack));
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
