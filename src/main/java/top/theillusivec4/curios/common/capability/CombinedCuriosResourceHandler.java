package top.theillusivec4.curios.common.capability;

import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import top.theillusivec4.curios.api.common.inventory.CuriosResourceHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.impl.CuriosRegistry;

public class CombinedCuriosResourceHandler extends CombinedResourceHandler<ItemResource> {

  public static CombinedCuriosResourceHandler from(final LivingEntity livingEntity) {
    CurioInventory inv = livingEntity.getData(CuriosRegistry.INVENTORY.get());
    Map<String, ICurioStacksHandler> curios = inv.curios;
    CuriosResourceHandler[] wrappers = new CuriosResourceHandler[curios.size()];
    int index = 0;

    for (ICurioStacksHandler stacksHandler : curios.values()) {

      if (index < wrappers.length) {
        wrappers[index] = new CuriosResourceHandler(stacksHandler.getStacks());
        index++;
      }
    }
    return new CombinedCuriosResourceHandler(wrappers);
  }

  @SafeVarargs
  public CombinedCuriosResourceHandler(ResourceHandler<ItemResource>... handlers) {
    super(handlers);
  }
}
