package top.theillusivec4.curios.api.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.item.Item;

public class CuriosRendererRegistry {

  private static final Map<Item, ICurioRenderer> RENDERER_REGISTRY = new HashMap<>();

  public static void register(Item item, ICurioRenderer renderer) {
    RENDERER_REGISTRY.put(item, renderer);
  }

  public static Optional<ICurioRenderer> getRenderer(Item item) {
    return Optional.ofNullable(RENDERER_REGISTRY.get(item));
  }
}
