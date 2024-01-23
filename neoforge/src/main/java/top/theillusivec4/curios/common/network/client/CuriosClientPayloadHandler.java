package top.theillusivec4.curios.common.network.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.SlotModifiersUpdatedEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.common.data.CuriosEntityManager;
import top.theillusivec4.curios.common.data.CuriosSlotManager;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;
import top.theillusivec4.curios.common.network.server.SPacketBreak;
import top.theillusivec4.curios.common.network.server.SPacketGrabbedItem;
import top.theillusivec4.curios.common.network.server.SPacketScroll;
import top.theillusivec4.curios.common.network.server.SPacketSetIcons;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncCurios;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncData;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncModifiers;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncRender;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack;
import top.theillusivec4.curios.server.command.CurioArgumentType;

public class CuriosClientPayloadHandler {

  private static final CuriosClientPayloadHandler INSTANCE = new CuriosClientPayloadHandler();

  public static CuriosClientPayloadHandler getInstance() {
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

  public void handleSetIcons(final SPacketSetIcons data, final PlayPayloadContext ctx) {
    handleData(ctx, () -> {
      ClientLevel world = Minecraft.getInstance().level;
      Set<String> slotIds = new HashSet<>();

      if (world != null) {
        CuriosApi.getIconHelper().clearIcons();
        Map<String, ResourceLocation> icons = new HashMap<>();

        for (Map.Entry<String, ResourceLocation> entry : data.map.entrySet()) {
          CuriosApi.getIconHelper().addIcon(entry.getKey(), entry.getValue());
          icons.put(entry.getKey(), entry.getValue());
          slotIds.add(entry.getKey());
        }
        CuriosSlotManager.INSTANCE.setIcons(icons);
      }
      CurioArgumentType.slotIds = slotIds;
    });
  }

  public void handleScroll(final SPacketScroll data, final PlayPayloadContext ctx) {
    handleData(ctx, () -> {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer clientPlayer = mc.player;
      Screen screen = mc.screen;

      if (clientPlayer != null) {
        AbstractContainerMenu container = clientPlayer.containerMenu;

        if (container instanceof CuriosContainer && container.containerId == data.windowId()) {
          ((CuriosContainer) container).scrollToIndex(data.index());
        }
      }

      if (screen instanceof CuriosScreen) {
        ((CuriosScreen) screen).updateRenderButtons();
      }
    });
  }

  public void handleBreak(final SPacketBreak data, final PlayPayloadContext ctx) {
    handleData(ctx, () -> {
      ClientLevel world = Minecraft.getInstance().level;

      if (world != null) {
        Entity entity = world.getEntity(data.entityId());

        if (entity instanceof LivingEntity livingEntity) {
          CuriosApi.getCuriosInventory(livingEntity)
              .flatMap(handler -> handler.getStacksHandler(data.curioId())).ifPresent(stacks -> {
                ItemStack stack = stacks.getStacks().getStackInSlot(data.slotId());
                Optional<ICurio> possibleCurio = CuriosApi.getCurio(stack);
                NonNullList<Boolean> renderStates = stacks.getRenders();
                possibleCurio.ifPresent(curio -> curio.curioBreak(
                    new SlotContext(data.curioId(), livingEntity, data.slotId(), false,
                        renderStates.size() > data.slotId() && renderStates.get(data.slotId()))));

                if (possibleCurio.isEmpty()) {
                  ICurio.playBreakAnimation(stack, livingEntity);
                }
              });
        }
      }
    });
  }

  public void handleSyncRender(final SPacketSyncRender data, final PlayPayloadContext ctx) {
    handleData(ctx, () -> {
      ClientLevel world = Minecraft.getInstance().level;

      if (world != null) {
        Entity entity = world.getEntity(data.entityId());

        if (entity instanceof LivingEntity) {
          CuriosApi.getCuriosInventory((LivingEntity) entity)
              .flatMap(handler -> handler.getStacksHandler(data.curioId()))
              .ifPresent(stacksHandler -> {
                int index = data.slotId();
                NonNullList<Boolean> renderStatuses = stacksHandler.getRenders();

                if (renderStatuses.size() > index) {
                  renderStatuses.set(index, data.value());
                }
              });
        }
      }
    });
  }

  public void handleSyncModifiers(final SPacketSyncModifiers data, final PlayPayloadContext ctx) {
    handleData(ctx, () -> {
      ClientLevel world = Minecraft.getInstance().level;

      if (world != null) {
        Entity entity = world.getEntity(data.entityId);

        if (entity instanceof LivingEntity livingEntity) {
          CuriosApi.getCuriosInventory(livingEntity)
              .ifPresent(handler -> {
                Map<String, ICurioStacksHandler> curios = handler.getCurios();

                for (Map.Entry<String, CompoundTag> entry : data.updates.entrySet()) {
                  String id = entry.getKey();
                  ICurioStacksHandler stacksHandler = curios.get(id);

                  if (stacksHandler != null) {
                    stacksHandler.applySyncTag(entry.getValue());
                  }
                }

                if (!data.updates.isEmpty()) {
                  NeoForge.EVENT_BUS.post(
                      new SlotModifiersUpdatedEvent(livingEntity, data.updates.keySet()));
                }

                if (entity instanceof LocalPlayer localPlayer) {

                  if (localPlayer.containerMenu instanceof CuriosContainer) {
                    ((CuriosContainer) localPlayer.containerMenu).resetSlots();
                  }
                }
              });
        }
      }
    });
  }

  public void handleSyncData(final SPacketSyncData data, final PlayPayloadContext ctx) {
    handleData(ctx, () -> CuriosEntityManager.applySyncPacket(data.data));
  }

  public void handleSyncCurios(final SPacketSyncCurios data, final PlayPayloadContext ctx) {
    handleData(ctx, () -> {
      ClientLevel world = Minecraft.getInstance().level;

      if (world != null) {
        Entity entity = world.getEntity(data.entityId);

        if (entity instanceof LivingEntity) {
          CuriosApi.getCuriosInventory((LivingEntity) entity)
              .ifPresent(handler -> {
                Map<String, ICurioStacksHandler> stacks = new LinkedHashMap<>();

                for (Map.Entry<String, CompoundTag> entry : data.map.entrySet()) {
                  ICurioStacksHandler stacksHandler =
                      new CurioStacksHandler(handler, entry.getKey());
                  stacksHandler.applySyncTag(entry.getValue());
                  stacks.put(entry.getKey(), stacksHandler);
                }
                handler.setCurios(stacks);

                if (entity instanceof LocalPlayer localPlayer &&
                    localPlayer.containerMenu instanceof CuriosContainer curiosContainer) {
                  curiosContainer.resetSlots();
                }
              });
        }
      }
    });
  }

  public void handleGrabbedItem(final SPacketGrabbedItem data, final PlayPayloadContext ctx) {
    handleData(ctx, () -> {
      LocalPlayer clientPlayer = Minecraft.getInstance().player;

      if (clientPlayer != null) {
        clientPlayer.containerMenu.setCarried(data.stack());
      }
    });
  }

  public void handleSyncStack(final SPacketSyncStack data, final PlayPayloadContext ctx) {
    handleData(ctx, () -> {
      ClientLevel world = Minecraft.getInstance().level;

      if (world != null) {
        Entity entity = world.getEntity(data.entityId());

        if (entity instanceof LivingEntity livingEntity) {
          CuriosApi.getCuriosInventory(livingEntity)
              .flatMap(handler -> handler.getStacksHandler(data.curioId()))
              .ifPresent(stacksHandler -> {
                ItemStack stack = data.stack();
                CompoundTag compoundNBT = data.compoundTag();
                int slot = data.slotId();
                boolean cosmetic = SPacketSyncStack.HandlerType.fromValue(data.handlerType()) ==
                    SPacketSyncStack.HandlerType.COSMETIC;

                if (!compoundNBT.isEmpty()) {
                  NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                  CuriosApi.getCurio(stack).ifPresent(curio -> curio.readSyncData(
                      new SlotContext(data.curioId(), livingEntity, slot, cosmetic,
                          renderStates.size() > slot && renderStates.get(slot)), compoundNBT));
                }

                if (cosmetic) {
                  stacksHandler.getCosmeticStacks().setStackInSlot(slot, stack);
                } else {
                  stacksHandler.getStacks().setStackInSlot(slot, stack);
                }
              });
        }
      }
    });
  }
}
