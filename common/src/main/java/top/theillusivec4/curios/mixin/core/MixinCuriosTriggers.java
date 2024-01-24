package top.theillusivec4.curios.mixin.core;

import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosTriggers;
import top.theillusivec4.curios.common.util.EquipCurioTrigger;

@Mixin(value = CuriosTriggers.class, remap = false)
public class MixinCuriosTriggers {

  @Inject(at = @At("HEAD"), method = "equip", cancellable = true)
  private static void curios$equip(ItemPredicate.Builder itemPredicate,
                                   CallbackInfoReturnable<Criterion<EquipCurioTrigger.TriggerInstance>> cir) {
    cir.setReturnValue(EquipCurioTrigger.INSTANCE.createCriterion(new EquipCurioTrigger.TriggerInstance(
        Optional.empty(), Optional.of(itemPredicate.build()), Optional.empty())));
  }

  @Inject(at = @At("HEAD"), method = "equipAtLocation", cancellable = true)
  private static void curios$equipAtLocation(ItemPredicate.Builder itemPredicate,
                                             LocationPredicate.Builder locationPredicate,
                                             CallbackInfoReturnable<Criterion<EquipCurioTrigger.TriggerInstance>> cir) {
    cir.setReturnValue(EquipCurioTrigger.INSTANCE.createCriterion(new EquipCurioTrigger.TriggerInstance(
        Optional.empty(), Optional.of(itemPredicate.build()), Optional.of(locationPredicate.build()))));
  }
}
