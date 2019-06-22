/*
 * Copyright (C) 2018-2019  C4
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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.CuriosRegistry;
import top.theillusivec4.curios.api.imc.CurioIMCMessage;
import top.theillusivec4.curios.client.EventHandlerClient;
import top.theillusivec4.curios.client.KeyRegistry;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.client.gui.GuiEventHandler;
import top.theillusivec4.curios.client.render.LayerCurios;
import top.theillusivec4.curios.common.CommandCurios;
import top.theillusivec4.curios.common.CuriosConfig;
import top.theillusivec4.curios.common.CuriosInternalRegistry;
import top.theillusivec4.curios.common.capability.CapCurioInventory;
import top.theillusivec4.curios.common.capability.CapCurioItem;
import top.theillusivec4.curios.common.event.EventHandlerCurios;
import top.theillusivec4.curios.common.network.NetworkHandler;

import java.util.Map;

@Mod(Curios.MODID)
public class Curios {

    public static final String MODID = "curios";

    private static final boolean DEBUG = false;

    public Curios() {
        final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::setup);
        eventBus.addListener(this::enqueue);
        eventBus.addListener(this::process);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CuriosConfig.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CuriosConfig.clientSpec);
    }

    private void setup(FMLCommonSetupEvent evt) {
        CapCurioInventory.register();
        CapCurioItem.register();
        NetworkHandler.register();
        MinecraftForge.EVENT_BUS.register(new EventHandlerCurios());
    }

    private void enqueue(InterModEnqueueEvent evt) {

        for (String id : CuriosConfig.COMMON.createCurios.get()) {
            send(CuriosAPI.IMC.REGISTER_TYPE, new CurioIMCMessage(id));
        }

        for (String id : CuriosConfig.COMMON.disabledCurios.get()) {
            send(CuriosAPI.IMC.MODIFY_TYPE, new CurioIMCMessage(id).setEnabled(false));
        }

        if (DEBUG) {
            send(CuriosAPI.IMC.REGISTER_TYPE, new CurioIMCMessage("ring").setSize(10));
            send(CuriosAPI.IMC.REGISTER_TYPE, new CurioIMCMessage("necklace"));
        }
    }

    private void process(InterModProcessEvent evt) {
        CuriosRegistry.processCurioTypes(evt.getIMCStream(CuriosAPI.IMC.REGISTER_TYPE::equals), evt.getIMCStream(CuriosAPI.IMC.MODIFY_TYPE::equals));
    }

    private static void send(String id, Object msg) {
        InterModComms.sendTo(MODID, id, () -> msg);
    }

    private void onServerStarting(FMLServerStartingEvent evt) {
        CommandCurios.register(evt.getCommandDispatcher());
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientProxy {

        @SubscribeEvent
        public static void setupClient(FMLClientSetupEvent evt) {
            MinecraftForge.EVENT_BUS.register(new EventHandlerClient());
            MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
            MinecraftForge.EVENT_BUS.addListener(ClientProxy::onTextureStitch);
            ScreenManager.registerFactory(CuriosInternalRegistry.CONTAINER_TYPE, CuriosScreen::new);
            KeyRegistry.registerKeys();
            CuriosAPI.registerIcon("ring", new ResourceLocation(MODID, "item/empty_ring_slot"));
            CuriosAPI.registerIcon("necklace", new ResourceLocation(MODID, "item/empty_necklace_slot"));
            CuriosAPI.registerIcon("body", new ResourceLocation(MODID, "item/empty_body_slot"));
            CuriosAPI.registerIcon("back", new ResourceLocation(MODID, "item/empty_back_slot"));
            CuriosAPI.registerIcon("head", new ResourceLocation(MODID, "item/empty_head_slot"));
            CuriosAPI.registerIcon("belt", new ResourceLocation(MODID, "item/empty_belt_slot"));
            CuriosAPI.registerIcon("charm", new ResourceLocation(MODID, "item/empty_charm_slot"));
        }

        @SubscribeEvent
        public static void postSetupClient(FMLLoadCompleteEvent evt) {
            Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();

            for (PlayerRenderer render : skinMap.values()) {
                render.addLayer(new LayerCurios(render));
            }
        }

        public static void onTextureStitch(TextureStitchEvent.Pre evt) {
//            AtlasTexture map = evt.getMap();
//            IResourceManager manager = Minecraft.getInstance().getResourceManager();
//            CuriosInternalRegistry.processIcons();
//
//            for (ResourceLocation resource : CuriosAPI.getIcons().values()) {
//                map.registerSprite(manager, resource);
//            }
//            map.registerSprite(manager, new ResourceLocation("curios:item/empty_generic_slot"));
        }
    }
}
