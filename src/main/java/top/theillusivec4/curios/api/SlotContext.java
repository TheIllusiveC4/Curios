package top.theillusivec4.curios.api;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;

public record SlotContext(String identifier, LivingEntity entity, int index, boolean cosmetic,
                          boolean visible) {

  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  public String getIdentifier() {
    return identifier;
  }

  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  public int getIndex() {
    return index;
  }

  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  public LivingEntity getWearer() {
    return entity;
  }
}
