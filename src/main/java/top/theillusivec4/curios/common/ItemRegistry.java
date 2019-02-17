package top.theillusivec4.curios.common;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.common.item.ItemAmulet;
import top.theillusivec4.curios.common.item.ItemRing;

@Mod.EventBusSubscriber(modid = Curios.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemRegistry {

    @ObjectHolder("curios:ring")
    public static final Item RING = null;

    @ObjectHolder("curios:amulet")
    public static final Item AMULET = null;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(new ItemRing(), new ItemAmulet());
    }
}
