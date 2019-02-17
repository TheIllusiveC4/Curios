package c4.curios.proxy;

import c4.curios.Curios;
import c4.curios.client.GuiEventHandler;
import c4.curios.client.KeyRegistry;
import c4.curios.client.LayerCurios;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy implements IProxy {

    public void init(FMLInitializationEvent evt) {
        MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
        MinecraftForge.EVENT_BUS.register(new c4.curios.client.EventHandlerClient());
    }

    public void postInit(FMLPostInitializationEvent evt) {

    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent evt) {
        ModelLoader.setCustomModelResourceLocation(Curios.ring, 0, new ModelResourceLocation(Curios.ring
                .getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Curios.amulet, 0, new ModelResourceLocation(Curios.amulet
                .getRegistryName(), "inventory"));
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre evt) {
        evt.getMap().registerSprite(new ResourceLocation("curios:items/empty_ring_slot"));
        evt.getMap().registerSprite(new ResourceLocation("curios:items/empty_amulet_slot"));
        evt.getMap().registerSprite(new ResourceLocation("curios:items/empty_generic_slot"));
    }
}
