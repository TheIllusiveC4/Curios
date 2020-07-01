package top.theillusivec4.curios.api.type;

import net.minecraft.util.ResourceLocation;

public interface ISlotType extends Comparable<ISlotType> {

  /**
   * @return The identifier for this slot type
   */
  String getIdentifier();

  /**
   * @return The {@link ResourceLocation} for the icon associated with this slot type
   */
  ResourceLocation getIcon();

  /**
   * @return The priority of this slot type for ordering
   */
  int getPriority();

  /**
   * @return The number of slots to give by default for this slot type
   */
  int getSize();

  /**
   * @return True if the slot type should be locked by default and not usable until unlocked, false
   * otherwise
   */
  boolean isLocked();

  /**
   * @return True if the slot type should be visible, false otherwise
   */
  boolean isVisible();

  /**
   * @return True if the slot type has active cosmetic slots, false otherwise
   */
  boolean hasCosmetic();
}
