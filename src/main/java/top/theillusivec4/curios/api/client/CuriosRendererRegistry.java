package top.theillusivec4.curios.api.client;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.item.Item;

public class CuriosRendererRegistry {

  private static final Map<Item, ICurioRenderer> RENDERER_REGISTRY = new ConcurrentHashMap<>();

  public static void register(Item item, ICurioRenderer renderer) {
    RENDERER_REGISTRY.put(item, renderer);
  }

  public static Optional<ICurioRenderer> getRenderer(Item item) {
    return Optional.ofNullable(RENDERER_REGISTRY.get(item));
  }

  public static Map<Item, ICurioRenderer> getRenderers() {
    return ImmutableMap.copyOf(RENDERER_REGISTRY);
  }
}
