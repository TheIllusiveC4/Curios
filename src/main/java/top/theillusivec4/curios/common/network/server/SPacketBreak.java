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
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class SPacketBreak {

  private int entityId;
  private int slotId;
  private String curioId;

  public SPacketBreak(int entityId, String curioId, int slotId) {
    this.entityId = entityId;
    this.slotId = slotId;
    this.curioId = curioId;
  }

  public static void encode(SPacketBreak msg, PacketBuffer buf) {
    buf.writeInt(msg.entityId);
    buf.writeString(msg.curioId);
    buf.writeInt(msg.slotId);
  }

  public static SPacketBreak decode(PacketBuffer buf) {
    return new SPacketBreak(buf.readInt(), buf.readString(25), buf.readInt());
  }

  public static void handle(SPacketBreak msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ClientWorld world = Minecraft.getInstance().world;

      if (world != null) {
        Entity entity = Minecraft.getInstance().world.getEntityByID(msg.entityId);

        if (entity instanceof LivingEntity) {
          LivingEntity livingEntity = (LivingEntity) entity;

          CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {
            ItemStack stack = handler.getStacksHandler(msg.curioId)
                .map(stacksHandler -> stacksHandler.getStacks().getStackInSlot(msg.slotId))
                .orElse(ItemStack.EMPTY);
            LazyOptional<ICurio> possibleCurio = CuriosApi.getCuriosHelper().getCurio(stack);
            possibleCurio.ifPresent(curio -> curio.curioBreak(stack, livingEntity));

            if (!possibleCurio.isPresent()) {
              ICurio.breakItem(stack, livingEntity);
            }
          });
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
