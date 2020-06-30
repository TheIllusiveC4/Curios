package top.theillusivec4.curios.api.type.util;

import net.minecraft.util.ResourceLocation;

public interface IIconHelper {

  void clearIcons();

  void addIcon(String identifier, ResourceLocation resourceLocation);

  /**
   * @param identifier The identifier of the {@link top.theillusivec4.curios.api.type.ISlotType}
   * @return The resource location of the icon registered to the identifier
   */
  ResourceLocation getIcon(String identifier);
}
