package top.theillusivec4.curios;

import c4.curios.client.GuiContainerCurios;
import c4.curios.common.CommonEventHandler;
import c4.curios.common.ConfigHandler;
import c4.curios.common.CurioEventHandler;
import c4.curios.common.inventory.ContainerCurios;
import c4.curios.common.item.ItemAmulet;
import c4.curios.common.item.ItemRing;
import c4.curios.common.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLPlayMessages;
import top.theillusivec4.curios.api.capability.CapCurioInventory;
import top.theillusivec4.curios.api.capability.CapCurioItem;
import top.theillusivec4.curios.client.CurioContainerHandler;
import top.theillusivec4.curios.client.EventHandlerClient;
import top.theillusivec4.curios.client.KeyRegistry;
import top.theillusivec4.curios.client.LayerCurios;
import top.theillusivec4.curios.common.network.NetworkHandler;

import java.util.Map;

@Mod(Curios.MODID)
public class Curios {

    public static final String MODID = "curios";

    @GameRegistry.ObjectHolder("curios:ring")
    public static final Item ring = null;

    @GameRegistry.ObjectHolder("curios:amulet")
    public static final Item amulet = null;

    public Curios() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::setup);
    }

    private void setup(FMLCommonSetupEvent evt) {
        CapCurioInventory.register();
        CapCurioItem.register();
        NetworkHandler.register();
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientProxy {

        @SubscribeEvent
        public static void setupClient(FMLClientSetupEvent evt) {
            MinecraftForge.EVENT_BUS.register(new EventHandlerClient());
            KeyRegistry.registerKeys();
            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> ClientProxy::registerContainerHandler);
        }

        @SubscribeEvent
        public static void postSetupClient(FMLLoadCompleteEvent evt) {
            Map<String, RenderPlayer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();

            for (RenderPlayer render : skinMap.values()) {
                render.addLayer(new LayerCurios());
            }
        }

        private static GuiScreen registerContainerHandler(FMLPlayMessages.OpenContainer msg) {

            if (msg.getId().equals(CurioContainerHandler.ID)) {
                EntityPlayerSP sp = Minecraft.getInstance().player;
                return new GuiContainerCurios(new ContainerCurios(sp.inventory, sp));
            }
            return null;
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        logger = evt.getModLog();
        CuriosAPI.createSlot("ring").setIcon(new ResourceLocation(MODID, "items/empty_ring_slot")).setSize(10);
        CuriosAPI.createSlot("amulet").setIcon(new ResourceLocation(MODID, "items/empty_amulet_slot"));
        MinecraftForge.EVENT_BUS.register(this);
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
        NetworkHandler.register();
        proxy.preInit(evt);
    }

    @EventHandler
    public void init(FMLInitializationEvent evt) {
        CapCurioInventory.register();
        CapCurioItem.register();
        MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
        MinecraftForge.EVENT_BUS.register(new CurioEventHandler());
        proxy.init(evt);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt) {

        for (String id : ConfigHandler.disabledCurios) {
            CuriosAPI.setSlotEnabled(id, false);
        }
        proxy.postInit(evt);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> evt) {
        evt.getRegistry().registerAll(new ItemRing(), new ItemAmulet());
    }
}
