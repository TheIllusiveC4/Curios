package top.theillusivec4.curios.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import top.theillusivec4.curios.api.type.util.IIconHelper;

public class IconHelper implements IIconHelper {

  private Map<String, ResourceLocation> idToIcon = new HashMap<>();

  @Override
  public void clearIcons() {
    this.idToIcon.clear();
  }

  @Override
  public void addIcon(String identifier, ResourceLocation resourceLocation) {
    this.idToIcon.putIfAbsent(identifier, resourceLocation);
  }

  @Override
  public ResourceLocation getIcon(String identifier) {
    return idToIcon.getOrDefault(identifier, new ResourceLocation("item/empty_curio_slot"));
  }
}
