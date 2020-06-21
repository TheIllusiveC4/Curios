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

import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.client.CuriosClientConfig;
import top.theillusivec4.curios.client.EventHandlerClient;
import top.theillusivec4.curios.client.KeyRegistry;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.client.gui.GuiEventHandler;
import top.theillusivec4.curios.client.render.CuriosLayer;
import top.theillusivec4.curios.common.CuriosRegistry;
import top.theillusivec4.curios.common.capability.CurioInventoryCapability;
import top.theillusivec4.curios.common.capability.CurioItemCapability;
import top.theillusivec4.curios.common.event.CuriosEventHandler;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.server.command.CurioArgumentType;
import top.theillusivec4.curios.server.command.CuriosCommand;

@Mod(Curios.MODID)
public class Curios {

  public static final String MODID = CuriosApi.MODID;
  public static final Logger LOGGER = LogManager.getLogger();

  public Curios() {
    final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
    eventBus.addListener(this::setup);
    MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CuriosClientConfig.clientSpec);
  }

  private void setup(FMLCommonSetupEvent evt) {
    CurioInventoryCapability.register();
    CurioItemCapability.register();
    MinecraftForge.EVENT_BUS.register(new CuriosEventHandler());
    NetworkHandler.register();
    ArgumentTypes.register("curios:slot_type", CurioArgumentType.class,
        new ArgumentSerializer<>(CurioArgumentType::slot));
  }

  private void onServerStarting(FMLServerStartingEvent evt) {
    CuriosCommand.register(evt.getCommandDispatcher());
  }

  @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
  public static class ClientProxy {

    @SubscribeEvent
    public static void stitchTextures(TextureStitchEvent.Pre evt) {

      if (evt.getMap().getTextureLocation() == PlayerContainer.LOCATION_BLOCKS_TEXTURE) {
        String[] icons = new String[]{"charm", "necklace", "belt", "head", "back", "body", "hands",
            "ring", "generic"};

        for (String icon : icons) {
          evt.addSprite(new ResourceLocation(MODID, "item/empty_" + icon + "_slot"));
        }
      }
    }

    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent evt) {
      MinecraftForge.EVENT_BUS.register(new EventHandlerClient());
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
