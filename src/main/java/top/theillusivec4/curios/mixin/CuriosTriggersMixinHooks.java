package top.theillusivec4.curios.mixin;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import top.theillusivec4.curios.common.util.EquipCurioTrigger;

public class CuriosTriggersMixinHooks {

  public static CriterionTriggerInstance equip(ItemPredicate.Builder itemPredicate) {
    return new EquipCurioTrigger.Instance(ContextAwarePredicate.ANY, itemPredicate.build(),
        LocationPredicate.ANY);
  }

  public static CriterionTriggerInstance equipAtLocation(ItemPredicate.Builder itemPredicate,
                                                         LocationPredicate.Builder locationPredicate) {
    return new EquipCurioTrigger.Instance(ContextAwarePredicate.ANY, itemPredicate.build(),
        locationPredicate.build());
  }
}
