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

package top.theillusivec4.curios.common.network.server.sync;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

public class SPacketSyncStack {

  private int entityId;
  private int slotId;
  private String curioId;
  private ItemStack stack;
  private int handlerType;
  private CompoundTag compound;

  public SPacketSyncStack(int entityId, String curioId, int slotId, ItemStack stack,
                          HandlerType handlerType, CompoundTag data) {
    this.entityId = entityId;
    this.slotId = slotId;
    this.stack = stack.copy();
    this.curioId = curioId;
    this.handlerType = handlerType.ordinal();
    this.compound = data;
  }

  public static void encode(SPacketSyncStack msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.entityId);
    buf.writeUtf(msg.curioId);
    buf.writeInt(msg.slotId);
    buf.writeItem(msg.stack);
    buf.writeInt(msg.handlerType);
    buf.writeNbt(msg.compound);
  }

  public static SPacketSyncStack decode(FriendlyByteBuf buf) {
    return new SPacketSyncStack(buf.readInt(), buf.readUtf(25), buf.readInt(),
        buf.readItem(), HandlerType.fromValue(buf.readInt()), buf.readNbt());
  }

  public static void handle(SPacketSyncStack msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ClientLevel world = Minecraft.getInstance().level;

      if (world != null) {
        Entity entity = world.getEntity(msg.entityId);

        if (entity instanceof LivingEntity) {
          CuriosApi.getCuriosHelper().getCuriosHandler((LivingEntity) entity).ifPresent(
              handler -> handler.getStacksHandler(msg.curioId).ifPresent(stacksHandler -> {
                ItemStack stack = msg.stack;
                CompoundTag compoundNBT = msg.compound;
                int slot = msg.slotId;
                boolean cosmetic = HandlerType.fromValue(msg.handlerType) == HandlerType.COSMETIC;

                if (!compoundNBT.isEmpty()) {
                  NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                  CuriosApi.getCuriosHelper().getCurio(stack).ifPresent(curio -> curio.readSyncData(
                      new SlotContext(msg.curioId, (LivingEntity) entity, slot, cosmetic,
                          renderStates.size() > slot && renderStates.get(slot)), compoundNBT));
                }

                if (cosmetic) {
                  stacksHandler.getCosmeticStacks().setStackInSlot(slot, stack);
                } else {
                  stacksHandler.getStacks().setStackInSlot(slot, stack);
                }
              }));
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }

  public enum HandlerType {
    EQUIPMENT, COSMETIC;

    public static HandlerType fromValue(int value) {
      try {
        return HandlerType.values()[value];
      } catch (ArrayIndexOutOfBoundsException e) {
        throw new IllegalArgumentException("Unknown handler value: " + value);
      }
    }
  }
}
