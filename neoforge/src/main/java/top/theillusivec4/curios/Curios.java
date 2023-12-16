/*
 * Copyright (c) 2018-2023 C4
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

package top.theillusivec4.curios;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.client.ClientEventHandler;
import top.theillusivec4.curios.client.CuriosClientConfig;
import top.theillusivec4.curios.client.IconHelper;
import top.theillusivec4.curios.client.KeyRegistry;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.client.gui.GuiEventHandler;
import top.theillusivec4.curios.client.render.CuriosLayer;
import top.theillusivec4.curios.common.CuriosConfig;
import top.theillusivec4.curios.common.CuriosHelper;
import top.theillusivec4.curios.common.CuriosRegistry;
import top.theillusivec4.curios.common.capability.CurioInventoryCapability;
import top.theillusivec4.curios.common.capability.CurioItemHandler;
import top.theillusivec4.curios.common.capability.ItemizedCurioCapability;
import top.theillusivec4.curios.common.data.CuriosEntityManager;
import top.theillusivec4.curios.common.data.CuriosSlotManager;
import top.theillusivec4.curios.common.event.CuriosEventHandler;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.slottype.LegacySlotManager;
import top.theillusivec4.curios.mixin.CuriosImplMixinHooks;
import top.theillusivec4.curios.server.SlotHelper;
import top.theillusivec4.curios.server.command.CurioArgumentType;
import top.theillusivec4.curios.server.command.CuriosCommand;
import top.theillusivec4.curios.server.command.CuriosSelectorOptions;

@Mod(CuriosConstants.MOD_ID)
public class Curios {

  public Curios() {
    CuriosRegistry.init();
    final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
    eventBus.addListener(this::setup);
    eventBus.addListener(this::process);
    eventBus.addListener(this::registerCaps);
    NeoForge.EVENT_BUS.addListener(this::serverAboutToStart);
    NeoForge.EVENT_BUS.addListener(this::serverStopped);
    NeoForge.EVENT_BUS.addListener(this::registerCommands);
    NeoForge.EVENT_BUS.addListener(this::reload);
    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CuriosClientConfig.CLIENT_SPEC);
    ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CuriosConfig.SERVER_SPEC);
  }

  private void setup(FMLCommonSetupEvent evt) {
    CuriosApi.setCuriosHelper(new CuriosHelper());
    NeoForge.EVENT_BUS.register(new CuriosEventHandler());
    NetworkHandler.register();
    evt.enqueueWork(CuriosSelectorOptions::register);
  }

  private void registerCaps(RegisterCapabilitiesEvent evt) {

    for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {

      evt.registerEntity(CuriosCapability.ITEM_HANDLER, entityType,
          (entity, ctx) -> {

            if (entity instanceof LivingEntity livingEntity) {
              Level level = livingEntity.level();
              EntityType<?> type = livingEntity.getType();

              if (!level.isClientSide() &&
                  CuriosApi.getEntitySlots(type).isEmpty()) {
                return null;
              }

              if (level.isClientSide() &&
                  !CuriosEntityManager.INSTANCE.hasSlots(type)) {
                return null;
              }
              return new CurioItemHandler(livingEntity);
            }
            return null;
          });

      evt.registerEntity(CuriosCapability.INVENTORY, entityType,
          (entity, ctx) -> {

            if (entity instanceof LivingEntity livingEntity) {
              Level level = livingEntity.level();
              EntityType<?> type = livingEntity.getType();

              if (!level.isClientSide() &&
                  CuriosApi.getEntitySlots(type).isEmpty()) {
                return null;
              }

              if (level.isClientSide() &&
                  !CuriosEntityManager.INSTANCE.hasSlots(type)) {
                return null;
              }
              return new CurioInventoryCapability(livingEntity);
            }
            return null;
          });
    }

    for (Item item : BuiltInRegistries.ITEM) {
      evt.registerItem(CuriosCapability.ITEM, (stack, ctx) -> {
        Item it = stack.getItem();
        ICurioItem curioItem = CuriosImplMixinHooks.getCurioFromRegistry(item).orElse(null);

        if (curioItem == null && it instanceof ICurioItem itemCurio) {
          curioItem = itemCurio;
        }

        if (curioItem != null && curioItem.hasCurioCapability(stack)) {
          return new ItemizedCurioCapability(curioItem, stack);
        }
        return null;
      }, item);
    }
  }

  private void process(InterModProcessEvent evt) {
    LegacySlotManager.buildImcSlotTypes(evt.getIMCStream(SlotTypeMessage.REGISTER_TYPE::equals),
        evt.getIMCStream(SlotTypeMessage.MODIFY_TYPE::equals));
  }

  private void serverAboutToStart(ServerAboutToStartEvent evt) {
    CuriosApi.setSlotHelper(new SlotHelper());
    Set<String> slotIds = new HashSet<>();

    for (ISlotType value : CuriosSlotManager.INSTANCE.getSlots().values()) {
      CuriosApi.getSlotHelper().addSlotType(value);
      slotIds.add(value.getIdentifier());
    }
    CurioArgumentType.slotIds = slotIds;
  }

  private void serverStopped(ServerStoppedEvent evt) {
    CuriosApi.setSlotHelper(null);
  }

  private void registerCommands(RegisterCommandsEvent evt) {
    CuriosCommand.register(evt.getDispatcher(), evt.getBuildContext());
  }

  private void reload(final AddReloadListenerEvent evt) {
    ICondition.IContext ctx = evt.getConditionContext();
    CuriosSlotManager.INSTANCE = new CuriosSlotManager(ctx);
    evt.addListener(CuriosSlotManager.INSTANCE);
    CuriosEntityManager.INSTANCE = new CuriosEntityManager(ctx);
    evt.addListener(CuriosEntityManager.INSTANCE);
    evt.addListener(new SimplePreparableReloadListener<Void>() {
      @Nonnull
      @Override
      protected Void prepare(@Nonnull ResourceManager resourceManagerIn,
                             @Nonnull ProfilerFiller profilerIn) {
        return null;
      }

      @Override
      protected void apply(@Nonnull Void objectIn, @Nonnull ResourceManager resourceManagerIn,
                           @Nonnull ProfilerFiller profilerIn) {
        CuriosEventHandler.dirtyTags = true;
      }
    });
  }

  @Mod.EventBusSubscriber(modid = CuriosConstants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
  public static class ClientProxy {

    @SubscribeEvent
    public static void registerKeys(final RegisterKeyMappingsEvent evt) {
      evt.register(KeyRegistry.openCurios);
    }

    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent evt) {
      CuriosApi.setIconHelper(new IconHelper());
      NeoForge.EVENT_BUS.register(new ClientEventHandler());
      NeoForge.EVENT_BUS.register(new GuiEventHandler());
      MenuScreens.register(CuriosRegistry.CURIO_MENU.get(), CuriosScreen::new);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers evt) {

      for (PlayerSkin.Model skin : evt.getSkins()) {
        addPlayerLayer(evt, skin);
      }
      CuriosRendererRegistry.load();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addPlayerLayer(EntityRenderersEvent.AddLayers evt, PlayerSkin.Model model) {
      EntityRenderer<? extends Player> renderer = evt.getSkin(model);

      if (renderer instanceof LivingEntityRenderer livingRenderer) {
        livingRenderer.addLayer(new CuriosLayer<>(livingRenderer));
      }
    }
  }
}
