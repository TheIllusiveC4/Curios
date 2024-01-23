package top.theillusivec4.curios.common.network.server;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;
import top.theillusivec4.curios.common.inventory.container.CuriosContainerProvider;
import top.theillusivec4.curios.common.network.client.CPacketDestroy;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;
import top.theillusivec4.curios.common.network.client.CPacketOpenVanilla;
import top.theillusivec4.curios.common.network.client.CPacketScroll;
import top.theillusivec4.curios.common.network.client.CPacketToggleRender;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncRender;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack;

public class CuriosServerPayloadHandler {

  private static final CuriosServerPayloadHandler INSTANCE = new CuriosServerPayloadHandler();

  public static CuriosServerPayloadHandler getInstance() {
    return INSTANCE;
  }

  private static void handleData(final PlayPayloadContext ctx, Runnable handler) {
    ctx.workHandler().submitAsync(handler)
        .exceptionally(e -> {
          ctx.packetHandler()
              .disconnect(Component.translatable("curios.networking.failed", e.getMessage()));
          return null;
        });
  }

  public void handlerToggleRender(final CPacketToggleRender data, final PlayPayloadContext ctx) {
    handleData(ctx, () -> ctx.player().ifPresent(player -> {
      CuriosApi.getCuriosInventory(player)
          .flatMap(handler -> handler.getStacksHandler(data.identifier()))
          .ifPresent(stacksHandler -> {
            NonNullList<Boolean> renderStatuses = stacksHandler.getRenders();

            if (renderStatuses.size() > data.index()) {
              boolean value = !renderStatuses.get(data.index());
              renderStatuses.set(data.index(), value);
              PacketDistributor.TRACKING_ENTITY_AND_SELF.with(player).send(
                  new SPacketSyncRender(player.getId(), data.identifier(), data.index(), value));
            }
          });
    }));
  }

  public void handleScroll(final CPacketScroll data, final PlayPayloadContext ctx) {
    handleData(ctx, () -> ctx.player().ifPresent(player -> {
      AbstractContainerMenu container = player.containerMenu;

      if (container instanceof CuriosContainer && container.containerId == data.windowId()) {
        ((CuriosContainer) container).scrollToIndex(data.index());
      }
    }));
  }

  public void handleOpenVanilla(final CPacketOpenVanilla data, final PlayPayloadContext ctx) {
    handleData(ctx, () -> ctx.player().ifPresent(player -> {

      if (player instanceof ServerPlayer serverPlayer) {
        ItemStack stack =
            player.isCreative() ? data.carried() : player.containerMenu.getCarried();
        player.containerMenu.setCarried(ItemStack.EMPTY);
        serverPlayer.doCloseContainer();

        if (!stack.isEmpty()) {

          if (!player.isCreative()) {
            player.containerMenu.setCarried(stack);
          }
          PacketDistributor.PLAYER.with(serverPlayer).send(new SPacketGrabbedItem(stack));
        }
      }
    }));
  }

  public void handleOpenCurios(final CPacketOpenCurios data, final PlayPayloadContext ctx) {
    handleData(ctx, () -> {
      ctx.player().ifPresent(player -> {

        if (player instanceof ServerPlayer serverPlayer) {
          ItemStack stack =
              player.isCreative() ? data.carried() : player.containerMenu.getCarried();
          player.containerMenu.setCarried(ItemStack.EMPTY);
          player.openMenu(new CuriosContainerProvider());

          if (!stack.isEmpty()) {
            player.containerMenu.setCarried(stack);
            PacketDistributor.PLAYER.with(serverPlayer).send(new SPacketGrabbedItem(stack));
          }
        }
      });
    });
  }

  public void handleDestroyPacket(final CPacketDestroy data, final PlayPayloadContext ctx) {
    handleData(ctx, () -> ctx.player().ifPresent(player -> CuriosApi.getCuriosInventory(player)
        .ifPresent(handler -> handler.getCurios().values().forEach(stacksHandler -> {
          IDynamicStackHandler stackHandler = stacksHandler.getStacks();
          IDynamicStackHandler cosmeticStackHandler = stacksHandler.getCosmeticStacks();
          String id = stacksHandler.getIdentifier();

          for (int i = 0; i < stackHandler.getSlots(); i++) {
            UUID uuid = UUID.nameUUIDFromBytes((id + i).getBytes());
            NonNullList<Boolean> renderStates = stacksHandler.getRenders();
            SlotContext slotContext = new SlotContext(id, player, i, false,
                renderStates.size() > i && renderStates.get(i));
            ItemStack stack = stackHandler.getStackInSlot(i);
            Multimap<Attribute, AttributeModifier> map =
                CuriosApi.getAttributeModifiers(slotContext, uuid, stack);
            Multimap<String, AttributeModifier> slots = HashMultimap.create();
            Set<SlotAttribute> toRemove = new HashSet<>();

            for (Attribute attribute : map.keySet()) {

              if (attribute instanceof SlotAttribute wrapper) {
                slots.putAll(wrapper.getIdentifier(), map.get(attribute));
                toRemove.add(wrapper);
              }
            }

            for (Attribute attribute : toRemove) {
              map.removeAll(attribute);
            }
            player.getAttributes().removeAttributeModifiers(map);
            handler.removeSlotModifiers(slots);
            CuriosApi.getCurio(stack)
                .ifPresent(curio -> curio.onUnequip(slotContext, stack));
            stackHandler.setStackInSlot(i, ItemStack.EMPTY);
            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(player)
                .send(new SPacketSyncStack(player.getId(), id, i, ItemStack.EMPTY,
                    SPacketSyncStack.HandlerType.EQUIPMENT.ordinal(), new CompoundTag()));
            cosmeticStackHandler.setStackInSlot(i, ItemStack.EMPTY);
            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(player)
                .send(new SPacketSyncStack(player.getId(), id, i, ItemStack.EMPTY,
                    SPacketSyncStack.HandlerType.COSMETIC.ordinal(), new CompoundTag()));
          }
        }))));
  }
}
