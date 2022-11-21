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

package top.theillusivec4.curios;

import com.electronwill.nightconfig.core.CommentedConfig;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.client.ClientEventHandler;
import top.theillusivec4.curios.client.CuriosClientConfig;
import top.theillusivec4.curios.client.CuriosClientMod;
import top.theillusivec4.curios.client.IconHelper;
import top.theillusivec4.curios.client.KeyRegistry;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.client.gui.GuiEventHandler;
import top.theillusivec4.curios.client.render.CuriosLayer;
import top.theillusivec4.curios.common.CuriosConfig;
import top.theillusivec4.curios.common.CuriosHelper;
import top.theillusivec4.curios.common.CuriosRegistry;
import top.theillusivec4.curios.common.capability.CurioInventoryCapability;
import top.theillusivec4.curios.common.capability.CurioItemCapability;
import top.theillusivec4.curios.common.event.CuriosEventHandler;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.slottype.SlotTypeManager;
import top.theillusivec4.curios.common.triggers.EquipCurioTrigger;
import top.theillusivec4.curios.server.SlotHelper;
import top.theillusivec4.curios.server.command.CurioArgumentType;
import top.theillusivec4.curios.server.command.CuriosCommand;
import top.theillusivec4.curios.server.command.CuriosSelectorOptions;

@Mod(Curios.MODID)
public class Curios {

  public static final String MODID = CuriosApi.MODID;
  public static final Logger LOGGER = LogManager.getLogger();

  private static final boolean DEBUG = true;

  public Curios() {
    DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> CuriosClientMod::init);
    final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
    eventBus.addListener(this::setup);
    eventBus.addListener(this::config);
    eventBus.addListener(this::enqueue);
    eventBus.addListener(this::process);
    MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
    MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
    MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    MinecraftForge.EVENT_BUS.addListener(this::reload);
    ModLoadingContext.get().registerConfig(Type.CLIENT, CuriosClientConfig.CLIENT_SPEC);
    ModLoadingContext.get().registerConfig(Type.SERVER, CuriosConfig.SERVER_SPEC);
  }

  private void setup(FMLCommonSetupEvent evt) {
    CuriosApi.setCuriosHelper(new CuriosHelper());
    CurioInventoryCapability.register();
    CurioItemCapability.register();
    MinecraftForge.EVENT_BUS.register(new CuriosEventHandler());
    NetworkHandler.register();
    evt.enqueueWork(() -> {
      ArgumentTypes.register("curios:slot_type", CurioArgumentType.class,
          new ArgumentSerializer<>(CurioArgumentType::slot));
      CriteriaTriggers.register(EquipCurioTrigger.INSTANCE);
      CuriosSelectorOptions.register();
    });
  }

  private void enqueue(InterModEnqueueEvent evt) {

    if (DEBUG) {
      InterModComms.sendTo(MODID, SlotTypeMessage.REGISTER_TYPE,
          () -> Arrays.stream(SlotTypePreset.values())
              .map(preset -> preset.getMessageBuilder().build()).collect(Collectors.toList()));
    }
  }

  private void process(InterModProcessEvent evt) {
    SlotTypeManager.buildImcSlotTypes(evt.getIMCStream(SlotTypeMessage.REGISTER_TYPE::equals),
        evt.getIMCStream(SlotTypeMessage.MODIFY_TYPE::equals));
  }

  private void serverAboutToStart(FMLServerAboutToStartEvent evt) {
    CuriosApi.setSlotHelper(new SlotHelper());
    SlotTypeManager.buildSlotTypes();
  }

  private void serverStopped(FMLServerStoppedEvent evt) {
    CuriosApi.setSlotHelper(null);
  }

  private void registerCommands(RegisterCommandsEvent evt) {
    CuriosCommand.register(evt.getDispatcher());
  }

  private void reload(final AddReloadListenerEvent evt) {
    evt.addListener(new ReloadListener<Void>() {
      @Nonnull
      @Override
      protected Void prepare(@Nonnull IResourceManager resourceManagerIn,
                             @Nonnull IProfiler profilerIn) {
        return null;
      }

      @Override
      protected void apply(@Nonnull Void objectIn, @Nonnull IResourceManager resourceManagerIn,
                           @Nonnull IProfiler profilerIn) {
        CuriosEventHandler.dirtyTags = true;
      }
    });
  }

  private void config(final ModConfig.Loading evt) {

    if (evt.getConfig().getModId().equals(MODID)) {

      if (evt.getConfig().getType() == Type.SERVER) {
        ForgeConfigSpec spec = evt.getConfig().getSpec();
        CommentedConfig commentedConfig = evt.getConfig().getConfigData();

        if (spec == CuriosConfig.SERVER_SPEC) {
          CuriosConfig.transformCurios(commentedConfig);
          SlotTypeManager.buildConfigSlotTypes();
        }
      }
    }
  }

  @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Bus.MOD)
  public static class ClientProxy {

    @SubscribeEvent
    public static void stitchTextures(TextureStitchEvent.Pre evt) {
      CuriosClientMod.stitch(evt);
    }

    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent evt) {
      CuriosApi.setIconHelper(new IconHelper());
      MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
      MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
      ScreenManager.registerFactory(CuriosRegistry.CONTAINER_TYPE, CuriosScreen::new);
      KeyRegistry.registerKeys();
    }

    @SubscribeEvent
    public static void postSetupClient(FMLLoadCompleteEvent evt) {
      Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();

      for (PlayerRenderer render : skinMap.values()) {
        render.addLayer(new CuriosLayer<>(render));
      }
    }
  }
}
