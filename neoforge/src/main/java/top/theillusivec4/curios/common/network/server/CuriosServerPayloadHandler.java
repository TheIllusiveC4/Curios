/*
 * Copyright (c) 2018-2024 C4
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
 *
 */

package top.theillusivec4.curios.common.network.server;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;
import top.theillusivec4.curios.common.inventory.container.CuriosContainerProvider;
import top.theillusivec4.curios.common.network.client.CPacketDestroy;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;
import top.theillusivec4.curios.common.network.client.CPacketOpenVanilla;
import top.theillusivec4.curios.common.network.client.CPacketPage;
import top.theillusivec4.curios.common.network.client.CPacketToggleCosmetics;
import top.theillusivec4.curios.common.network.client.CPacketToggleRender;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncRender;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack;

public class CuriosServerPayloadHandler {

  private static final CuriosServerPayloadHandler INSTANCE = new CuriosServerPayloadHandler();

  public static CuriosServerPayloadHandler getInstance() {
    return INSTANCE;
  }

  public void handlerToggleRender(final CPacketToggleRender data, final IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      Player player = ctx.player();
      CuriosApi.getCuriosInventory(player)
          .flatMap(handler -> handler.getStacksHandler(data.identifier()))
          .ifPresent(stacksHandler -> {
            NonNullList<Boolean> renderStatuses = stacksHandler.getRenders();

            if (renderStatuses.size() > data.index()) {
              boolean value = !renderStatuses.get(data.index());
              renderStatuses.set(data.index(), value);
              PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                  new SPacketSyncRender(player.getId(), data.identifier(), data.index(), value));
            }
          });
    });
  }

  public void handlePage(final CPacketPage data,
                         final IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      Player player = ctx.player();
      AbstractContainerMenu container = player.containerMenu;

      if (container instanceof CuriosContainer && container.containerId == data.windowId()) {

        if (data.next()) {
          ((CuriosContainer) container).nextPage();
        } else {
          ((CuriosContainer) container).prevPage();
        }
      }
    });
  }

  public void handlerToggleCosmetics(final CPacketToggleCosmetics data,
                                     final IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      Player player = ctx.player();
      AbstractContainerMenu container = player.containerMenu;

      if (container instanceof CuriosContainer && container.containerId == data.windowId()) {
        ((CuriosContainer) container).toggleCosmetics();
      }
    });
  }

  public void handleOpenVanilla(final CPacketOpenVanilla data, final IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      Player player = ctx.player();

      if (player instanceof ServerPlayer serverPlayer) {
        ItemStack stack =
            player.isCreative() ? data.carried() : player.containerMenu.getCarried();
        player.containerMenu.setCarried(ItemStack.EMPTY);
        serverPlayer.doCloseContainer();

        if (!stack.isEmpty()) {

          if (!player.isCreative()) {
            player.containerMenu.setCarried(stack);
          }
          PacketDistributor.sendToPlayer(serverPlayer, new SPacketGrabbedItem(stack));
        }
      }
    });
  }

  public void handleOpenCurios(final CPacketOpenCurios data, final IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      Player player = ctx.player();

      if (player instanceof ServerPlayer serverPlayer) {
        ItemStack stack =
            player.isCreative() ? data.carried() : player.containerMenu.getCarried();
        player.containerMenu.setCarried(ItemStack.EMPTY);
        player.openMenu(new CuriosContainerProvider());

        if (!stack.isEmpty()) {
          player.containerMenu.setCarried(stack);
          PacketDistributor.sendToPlayer(serverPlayer, new SPacketGrabbedItem(stack));
        }
      }
    });
  }

  public void handleDestroyPacket(final CPacketDestroy data, final IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      Player player = ctx.player();
      CuriosApi.getCuriosInventory(player)
          .ifPresent(handler -> handler.getCurios().values().forEach(stacksHandler -> {
            IDynamicStackHandler stackHandler = stacksHandler.getStacks();
            IDynamicStackHandler cosmeticStackHandler = stacksHandler.getCosmeticStacks();
            String id = stacksHandler.getIdentifier();

            for (int i = stackHandler.getSlots() - 1; i >= 0; i--) {
              NonNullList<Boolean> renderStates = stacksHandler.getRenders();
              SlotContext slotContext = new SlotContext(id, player, i, false,
                  renderStates.size() > i && renderStates.get(i));
              ItemStack stack = stackHandler.getStackInSlot(i);
              Multimap<Holder<Attribute>, AttributeModifier> map =
                  CuriosApi.getAttributeModifiers(slotContext, CuriosApi.getSlotId(slotContext),
                      stack);
              Multimap<String, AttributeModifier> slots = HashMultimap.create();
              Set<Holder<Attribute>> toRemove = new HashSet<>();
              AttributeMap attributeMap = player.getAttributes();

              for (Holder<Attribute> attribute : map.keySet()) {

                if (attribute.value() instanceof SlotAttribute wrapper) {
                  slots.putAll(wrapper.getIdentifier(), map.get(attribute));
                  toRemove.add(attribute);
                }
              }

              for (Holder<Attribute> attribute : toRemove) {
                map.removeAll(attribute);
              }

              map.forEach((key, value) -> {
                AttributeInstance attInst = attributeMap.getInstance(key);

                if (attInst != null) {
                  attInst.removeModifier(value);
                }
              });
              handler.removeSlotModifiers(slots);
              CuriosApi.getCurio(stack)
                  .ifPresent(curio -> curio.onUnequip(slotContext, stack));
              stackHandler.setStackInSlot(i, ItemStack.EMPTY);
              PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                  new SPacketSyncStack(player.getId(), id, i, ItemStack.EMPTY,
                      SPacketSyncStack.HandlerType.EQUIPMENT.ordinal(), new CompoundTag()));
              cosmeticStackHandler.setStackInSlot(i, ItemStack.EMPTY);
              PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                  new SPacketSyncStack(player.getId(), id, i, ItemStack.EMPTY,
                      SPacketSyncStack.HandlerType.COSMETIC.ordinal(), new CompoundTag()));
            }
          }));
    });
  }
}
