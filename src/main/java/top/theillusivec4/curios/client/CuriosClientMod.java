package top.theillusivec4.curios.client;

import com.google.common.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.NonNullList;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.CuriosConstants;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosResources;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.client.screen.CuriosScreen;
import top.theillusivec4.curios.client.screen.CuriosScreenEvents;
import top.theillusivec4.curios.impl.CuriosRegistry;
import top.theillusivec4.curios.impl.CuriosClientExtensions;

@Mod(value = CuriosConstants.MOD_ID, dist = Dist.CLIENT)
public class CuriosClientMod {

  public CuriosClientMod(final IEventBus eventBus, final ModContainer modContainer) {
    eventBus.addListener(this::registerKeys);
    eventBus.addListener(this::setupClient);
    eventBus.addListener(this::registerMenuScreens);
    eventBus.addListener(this::addEntityLayers);
    eventBus.addListener(this::registerRenderStateModifiers);
  }

  private void registerKeys(final RegisterKeyMappingsEvent evt) {
    evt.register(CuriosKeyMappings.OPEN_CURIOS_INVENTORY);
  }

  private void setupClient(final FMLClientSetupEvent evt) {
    NeoForge.EVENT_BUS.register(new CuriosClientEvents());
    NeoForge.EVENT_BUS.register(new CuriosScreenEvents());
  }

  private void registerMenuScreens(final RegisterMenuScreensEvent evt) {
    evt.register(CuriosRegistry.CURIO_MENU.get(), CuriosScreen::new);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void addEntityLayers(final EntityRenderersEvent.AddLayers evt) {
    EntityRendererProvider.Context context = evt.getContext();

    for (EntityType<?> entityType : evt.getEntityTypes()) {

      if (entityType != EntityType.PLAYER) {
        EntityRenderer<?, ?> renderer = evt.getRenderer(entityType);

        if (renderer instanceof LivingEntityRenderer livingRenderer) {
          livingRenderer.addLayer(new CuriosLayer<>(livingRenderer, context));
        }
      }
    }

    for (PlayerSkin.Model skin : evt.getSkins()) {
      EntityRenderer<? extends Player, ?> renderer = evt.getSkin(skin);

      if (renderer instanceof LivingEntityRenderer livingRenderer) {
        livingRenderer.addLayer(new CuriosLayer<>(livingRenderer, context));
      }
    }
    CuriosClientExtensions.loadRenderers();
  }

  public static final ContextKey<List<SlotResult>> CUSTOM_RENDER =
      new ContextKey<>(CuriosResources.resource("custom_render"));
  public static final ContextKey<List<SlotResult>> ARMOR_RENDER =
      new ContextKey<>(CuriosResources.resource("armor_render"));
  public static final ContextKey<List<SlotResult>> HANDHELD_RENDER =
      new ContextKey<>(CuriosResources.resource("handheld_render"));

  private void registerRenderStateModifiers(final RegisterRenderStateModifiersEvent evt) {
    evt.registerEntityModifier(
        new TypeToken<LivingEntityRenderer<? extends LivingEntity, LivingEntityRenderState, ?>>() {
        },
        (entity, renderState) -> {
          List<SlotResult> customSlots = new ArrayList<>();
          CuriosApi.getCuriosInventory(entity)
              .ifPresent(handler -> handler.getCurios().forEach((id, stacksHandler) -> {
                IDynamicStackHandler stackHandler = stacksHandler.getStacks();
                IDynamicStackHandler cosmeticStacksHandler = stacksHandler.getCosmeticStacks();

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                  ItemStack stack = cosmeticStacksHandler.getStackInSlot(i);
                  boolean cosmetic = true;
                  NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                  boolean renderable = renderStates.size() > i && renderStates.get(i);

                  if (stack.isEmpty() && renderable) {
                    stack = stackHandler.getStackInSlot(i);
                    cosmetic = false;
                  }

                  if (!stack.isEmpty()) {
                    SlotContext
                        slotContext = new SlotContext(id, entity, i, cosmetic, renderable);
                    customSlots.add(new SlotResult(slotContext, stack));
                  }
                }
              }));
          renderState.setRenderData(CUSTOM_RENDER, customSlots);
        });
  }
}
