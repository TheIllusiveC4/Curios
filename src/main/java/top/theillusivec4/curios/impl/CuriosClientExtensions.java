package top.theillusivec4.curios.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.internal.services.client.ICuriosClientExtensions;

public class CuriosClientExtensions implements ICuriosClientExtensions {

  private static final Map<Item, Supplier<ICurioRenderer>> REGISTERED_RENDERERS =
      new ConcurrentHashMap<>();
  private static final Map<Item, ICurioRenderer> LOADED_RENDERERS = new LinkedHashMap<>();

  @Override
  public void registerCurioRenderer(Item item, Supplier<ICurioRenderer> curioRenderer) {
    REGISTERED_RENDERERS.put(item, curioRenderer);
  }

  @Override
  public ICurioRenderer getCurioRenderer(Item item) {
    return LOADED_RENDERERS.get(item);
  }

  public static void loadRenderers() {

    for (Map.Entry<Item, Supplier<ICurioRenderer>> entry : REGISTERED_RENDERERS.entrySet()) {
      LOADED_RENDERERS.put(entry.getKey(), entry.getValue().get());
    }
  }
}
