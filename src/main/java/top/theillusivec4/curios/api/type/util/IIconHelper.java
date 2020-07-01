package top.theillusivec4.curios.api.type.util;

import net.minecraft.util.ResourceLocation;

public interface IIconHelper {

  /**
   * Clears all of the registered icons.
   */
  void clearIcons();

  /**
   * Adds a {@link ResourceLocation} for the icon to the given {@link
   * top.theillusivec4.curios.api.type.ISlotType} identifier
   *
   * @param identifier       The {@link top.theillusivec4.curios.api.type.ISlotType} identifier
   * @param resourceLocation The {@link ResourceLocation} for to the icon
   */
  void addIcon(String identifier, ResourceLocation resourceLocation);

  /**
   * @param identifier The identifier of the {@link top.theillusivec4.curios.api.type.ISlotType}
   * @return The resource location of the icon registered to the identifier
   */
  ResourceLocation getIcon(String identifier);
}
