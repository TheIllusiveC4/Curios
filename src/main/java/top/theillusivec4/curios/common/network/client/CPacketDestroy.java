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

package top.theillusivec4.curios.common.network.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.CuriosHelper;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack;

public class CPacketDestroy {

  public static void encode(CPacketDestroy msg, PacketBuffer buf) {
  }

  public static CPacketDestroy decode(PacketBuffer buf) {
    return new CPacketDestroy();
  }

  public static void handle(CPacketDestroy msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayerEntity sender = ctx.get().getSender();

      if (sender != null) {
        CuriosApi.getCuriosHelper().getCuriosHandler(sender)
            .ifPresent(handler -> handler.getCurios().values().forEach(stacksHandler -> {
              IDynamicStackHandler stackHandler = stacksHandler.getStacks();
              IDynamicStackHandler cosmeticStackHandler = stacksHandler.getCosmeticStacks();
              String id = stacksHandler.getIdentifier();

              for (int i = 0; i < stackHandler.getSlots(); i++) {
                UUID uuid = UUID.nameUUIDFromBytes((id + i).getBytes());
                SlotContext slotContext = new SlotContext(id, sender, i);
                ItemStack stack = stackHandler.getStackInSlot(i);
                Multimap<Attribute, AttributeModifier> map =
                    CuriosApi.getCuriosHelper().getAttributeModifiers(slotContext, uuid, stack);
                Multimap<String, AttributeModifier> slots = HashMultimap.create();
                Set<CuriosHelper.SlotAttributeWrapper> toRemove = new HashSet<>();

                for (Attribute attribute : map.keySet()) {

                  if (attribute instanceof CuriosHelper.SlotAttributeWrapper) {
                    CuriosHelper.SlotAttributeWrapper wrapper =
                        (CuriosHelper.SlotAttributeWrapper) attribute;
                    slots.putAll(wrapper.identifier, map.get(attribute));
                    toRemove.add(wrapper);
                  }
                }

                for (Attribute attribute : toRemove) {
                  map.removeAll(attribute);
                }
                sender.getAttributeManager().removeModifiers(map);
                handler.removeSlotModifiers(slots);
                CuriosApi.getCuriosHelper().getCurio(stack)
                    .ifPresent(curio -> curio.onUnequip(slotContext, stack));
                stackHandler.setStackInSlot(i, ItemStack.EMPTY);
                NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sender),
                    new SPacketSyncStack(sender.getEntityId(), id, i, ItemStack.EMPTY,
                        SPacketSyncStack.HandlerType.EQUIPMENT, new CompoundNBT()));
                cosmeticStackHandler.setStackInSlot(i, ItemStack.EMPTY);
                NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sender),
                    new SPacketSyncStack(sender.getEntityId(), id, i, ItemStack.EMPTY,
                        SPacketSyncStack.HandlerType.COSMETIC, new CompoundNBT()));
              }
            }));
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
