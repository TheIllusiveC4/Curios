package top.theillusivec4.curios.api;

import javax.annotation.Nonnull;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import top.theillusivec4.curios.common.util.EquipCurioTrigger;

public class CuriosTriggers {

  @Nonnull
  public static Criterion<EquipCurioTrigger.TriggerInstance> equip(
      ItemPredicate.Builder itemPredicate) {
    return new Criterion<>(null, null);
  }

  @Nonnull
  public static Criterion<EquipCurioTrigger.TriggerInstance> equipAtLocation(
      ItemPredicate.Builder itemPredicate, LocationPredicate.Builder locationPredicate) {
    return new Criterion<>(null, null);
  }
}
