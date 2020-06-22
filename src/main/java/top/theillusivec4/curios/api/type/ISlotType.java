package top.theillusivec4.curios.api.type;

import net.minecraft.util.ResourceLocation;

public interface ISlotType extends Comparable<ISlotType> {

  String getIdentifier();

  ResourceLocation getIcon();

  int getPriority();

  int getSize();

  boolean isLocked();

  boolean isVisible();

  boolean hasCosmetic();
}
