package top.theillusivec4.curios.common;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.inventory.screen.CuriosScreenHandler;
import top.theillusivec4.curios.common.item.AmuletItem;

public class CuriosRegistry {

  public static final Item AMULET = new AmuletItem();

  public static final ScreenHandlerType<CuriosScreenHandler> CURIOS_SCREENHANDLER = ScreenHandlerRegistry
      .registerSimple(new Identifier(CuriosApi.MODID, "curios_screen"), CuriosScreenHandler::new);

  public static void registerItems() {
    Registry.register(Registry.ITEM, new Identifier(CuriosApi.MODID, "amulet"), AMULET);
  }
}
