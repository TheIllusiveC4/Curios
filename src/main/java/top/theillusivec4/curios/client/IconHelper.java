package top.theillusivec4.curios.client;


import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Identifier;
import top.theillusivec4.curios.api.type.util.IIconHelper;

public class IconHelper implements IIconHelper {

  private Map<String, Identifier> idToIcon = new HashMap<>();

  @Override
  public void clearIcons() {
    this.idToIcon.clear();
  }

  @Override
  public void addIcon(String identifier, Identifier resourceLocation) {
    this.idToIcon.putIfAbsent(identifier, resourceLocation);
  }

  @Override
  public Identifier getIcon(String identifier) {
    return idToIcon.getOrDefault(identifier, new Identifier("item/empty_curio_slot"));
  }
}
