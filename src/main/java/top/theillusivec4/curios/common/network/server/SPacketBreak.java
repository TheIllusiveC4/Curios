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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
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

  public static void encode(SPacketBreak msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.entityId);
    buf.writeUtf(msg.curioId);
    buf.writeInt(msg.slotId);
  }

  public static SPacketBreak decode(FriendlyByteBuf buf) {
    return new SPacketBreak(buf.readInt(), buf.readUtf(), buf.readInt());
  }

  public static void handle(SPacketBreak msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ClientLevel world = Minecraft.getInstance().level;

      if (world != null) {
        Entity entity = Minecraft.getInstance().level.getEntity(msg.entityId);

        if (entity instanceof LivingEntity livingEntity) {
          CuriosApi.getCuriosInventory(livingEntity)
              .ifPresent(handler -> handler.getStacksHandler(msg.curioId).ifPresent(stacks -> {
                ItemStack stack = stacks.getStacks().getStackInSlot(msg.slotId);
                LazyOptional<ICurio> possibleCurio = CuriosApi.getCurio(stack);
                NonNullList<Boolean> renderStates = stacks.getRenders();
                possibleCurio.ifPresent(curio -> curio.curioBreak(
                    new SlotContext(msg.curioId, livingEntity, msg.slotId, false,
                        renderStates.size() > msg.slotId && renderStates.get(msg.slotId))));

                if (!possibleCurio.isPresent()) {
                  ICurio.playBreakAnimation(stack, livingEntity);
                }
              }));
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
