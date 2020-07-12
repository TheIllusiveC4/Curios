package top.theillusivec4.curios.common;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.ItemComponentCallbackV2;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosComponent;
import top.theillusivec4.curios.api.type.component.ICurio;
import top.theillusivec4.curios.common.inventory.screen.CuriosScreenHandler;
import top.theillusivec4.curios.common.item.AmuletItem;

public class CuriosRegistry {

  public static final Item AMULET = new AmuletItem();

  public static final ScreenHandlerType<CuriosScreenHandler> CURIOS_SCREENHANDLER = ScreenHandlerRegistry
      .registerSimple(new Identifier(CuriosApi.MODID, "curios_screen"), CuriosScreenHandler::new);

  public static void registerItems() {
    Registry.register(Registry.ITEM, new Identifier(CuriosApi.MODID, "amulet"), AMULET);

    ItemComponentCallbackV2.event(AMULET).register(
        ((item, itemStack, componentContainer) -> componentContainer
            .put(CuriosComponent.ITEM, new ICurio() {

              @Override
              public ComponentType<ICurio> getComponentType() {
                return CuriosComponent.ITEM;
              }
            })));
  }
}
