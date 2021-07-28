package top.theillusivec4.curios.api;

import net.minecraft.world.entity.LivingEntity;

public record SlotContext(String identifier, LivingEntity entity, int index, boolean cosmetic,
                          boolean visible) {

  @Deprecated
  public String getIdentifier() {
    return identifier;
  }

  @Deprecated
  public int getIndex() {
    return index;
  }

  @Deprecated
  public LivingEntity getWearer() {
    return entity;
  }
}
