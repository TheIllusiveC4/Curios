package c4.curios;

import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.CapCurioInventory;
import c4.curios.api.capability.CapCurioItem;
import c4.curios.client.GuiHandler;
import c4.curios.common.CommonEventHandler;
import c4.curios.common.CurioEventHandler;
import c4.curios.common.item.ItemAmulet;
import c4.curios.common.item.ItemRing;
import c4.curios.common.network.NetworkHandler;
import c4.curios.integration.crafttweaker.CuriosCrT;
import c4.curios.proxy.IProxy;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = Curios.MODID, name = Curios.NAME, version = Curios.VERSION)
public class Curios
{
    public static final String MODID = "curios";
    public static final String NAME = "Curios";
    public static final String VERSION = "0.0.1-alpha";

    @SidedProxy(clientSide = "c4.curios.proxy.ClientProxy", serverSide = "c4.curios.proxy.ServerProxy")
    public static IProxy proxy;

    @Mod.Instance
    public static Curios instance;

    public static Logger logger;

    @GameRegistry.ObjectHolder("curios:ring")
    public static final Item ring = null;

    @GameRegistry.ObjectHolder("curios:amulet")
    public static final Item amulet = null;

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        logger = evt.getModLog();
        CuriosAPI.createSlot("ring").setIcon(new ResourceLocation(MODID, "items/empty_ring_slot")).setSize(10);
        CuriosAPI.createSlot("amulet").setIcon(new ResourceLocation(MODID, "items/empty_amulet_slot"));
        MinecraftForge.EVENT_BUS.register(this);
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
        NetworkHandler.init();
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
        CuriosCrT.setOverrides();
        proxy.postInit(evt);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> evt) {
        evt.getRegistry().registerAll(new ItemRing(), new ItemAmulet());
    }
}
