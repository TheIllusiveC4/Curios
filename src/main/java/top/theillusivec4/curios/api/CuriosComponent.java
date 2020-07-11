package top.theillusivec4.curios.api;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import net.minecraft.util.Identifier;
import top.theillusivec4.curios.api.type.component.ICurio;
import top.theillusivec4.curios.api.type.component.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.component.IRenderableCurio;

public class CuriosComponent {

  public static final ComponentType<ICurio> ITEM = ComponentRegistry.INSTANCE
      .registerIfAbsent(new Identifier(CuriosApi.MODID, "item"), ICurio.class);

  public static final ComponentType<IRenderableCurio> ITEM_RENDER = ComponentRegistry.INSTANCE
      .registerIfAbsent(new Identifier(CuriosApi.MODID, "item_render"), IRenderableCurio.class);

  public static final ComponentType<ICuriosItemHandler> INVENTORY = ComponentRegistry.INSTANCE
      .registerIfAbsent(new Identifier(CuriosApi.MODID, "inventory"), ICuriosItemHandler.class);
}
