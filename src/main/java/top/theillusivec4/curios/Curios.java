package top.theillusivec4.curios;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.network.PacketBuffer;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLPlayMessages;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.CapCurioInventory;
import top.theillusivec4.curios.api.capability.CapCurioItem;
import top.theillusivec4.curios.client.*;
import top.theillusivec4.curios.client.gui.GuiContainerCurios;
import top.theillusivec4.curios.client.gui.GuiEventHandler;
import top.theillusivec4.curios.client.render.LayerCurios;
import top.theillusivec4.curios.common.CommonEventHandler;
import top.theillusivec4.curios.common.CurioEventHandler;
import top.theillusivec4.curios.common.CuriosConfig;
import top.theillusivec4.curios.common.inventory.ContainerCurios;
import top.theillusivec4.curios.common.inventory.CurioContainerHandler;
import top.theillusivec4.curios.common.network.NetworkHandler;

import java.util.Map;

@Mod(Curios.MODID)
public class Curios {

    public static final String MODID = "curios";

    public Curios() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CuriosConfig.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CuriosConfig.clientSpec);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> Curios::getGuiContainer);
        eventBus.addListener(this::setup);
        eventBus.addListener(this::postSetup);
    }

    private void setup(FMLCommonSetupEvent evt) {
        CapCurioInventory.register();
        CapCurioItem.register();
        NetworkHandler.register();
        MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
        MinecraftForge.EVENT_BUS.register(new CurioEventHandler());
        CuriosAPI.registerType("ring").setIcon(new ResourceLocation(MODID, "item/empty_ring_slot")).setSize(10);
        CuriosAPI.registerType("amulet").setIcon(new ResourceLocation(MODID, "item/empty_amulet_slot"));
    }

    private void postSetup(FMLLoadCompleteEvent evt) {

        for (String id : CuriosConfig.COMMON.disabledCurios.get()) {
            CuriosAPI.setTypeEnabled(id, false);
        }
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartedEvent evt) {
        CuriosAPI.refreshTags();
    }

    private static GuiScreen getGuiContainer(FMLPlayMessages.OpenContainer msg) {

        if (msg.getId().equals(CurioContainerHandler.ID)) {
            EntityPlayerSP sp = Minecraft.getInstance().player;
            return new GuiContainerCurios(new ContainerCurios(sp.inventory, sp));
        }
        return null;
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientProxy {

        @SubscribeEvent
        public static void setupClient(FMLClientSetupEvent evt) {
            MinecraftForge.EVENT_BUS.register(new EventHandlerClient());
            MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
            MinecraftForge.EVENT_BUS.addListener(ClientProxy::onTextureStitch);
            KeyRegistry.registerKeys();
        }

        @SubscribeEvent
        public static void postSetupClient(FMLLoadCompleteEvent evt) {
            Map<String, RenderPlayer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();

            for (RenderPlayer render : skinMap.values()) {
                render.addLayer(new LayerCurios());
            }
        }

        public static void onTextureStitch(TextureStitchEvent.Pre evt) {
            TextureMap map = evt.getMap();
            IResourceManager manager = Minecraft.getInstance().getResourceManager();
            map.registerSprite(manager, new ResourceLocation("curios:item/empty_ring_slot"));
            map.registerSprite(manager, new ResourceLocation("curios:item/empty_amulet_slot"));
            map.registerSprite(manager, new ResourceLocation("curios:item/empty_generic_slot"));
        }
    }
}
