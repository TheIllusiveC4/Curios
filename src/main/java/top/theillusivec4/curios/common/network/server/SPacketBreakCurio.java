/*
 * Copyright (c) 2018-2020 C4
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

package top.theillusivec4.curios.common.network.server;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.ICurio;

public class SPacketBreakCurio {

  private int entityId;
  private int slotId;
  private String curioId;

  public SPacketBreakCurio(int entityId, String curioId, int slotId) {
    this.entityId = entityId;
    this.slotId = slotId;
    this.curioId = curioId;
  }

  public static void encode(SPacketBreakCurio msg, PacketBuffer buf) {
    buf.writeInt(msg.entityId);
    buf.writeString(msg.curioId);
    buf.writeInt(msg.slotId);
  }

  public static SPacketBreakCurio decode(PacketBuffer buf) {
    return new SPacketBreakCurio(buf.readInt(), buf.readString(25), buf.readInt());
  }

  public static void handle(SPacketBreakCurio msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ClientWorld world = Minecraft.getInstance().world;

      if (world != null) {
        Entity entity = Minecraft.getInstance().world.getEntityByID(msg.entityId);

        if (entity instanceof LivingEntity) {
          LivingEntity livingEntity = (LivingEntity) entity;

          CuriosAPI.getCuriosHandler(livingEntity).ifPresent(handler -> {
            ItemStack stack = handler.getStackInSlot(msg.curioId, msg.slotId);
            LazyOptional<ICurio> possibleCurio = CuriosAPI.getCurio(stack);
            possibleCurio.ifPresent(curio -> curio.curioBreak(stack, livingEntity));

            if (!possibleCurio.isPresent()) {
              ICurio.playDefaultBreakSound(stack, livingEntity);
            }
          });
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
