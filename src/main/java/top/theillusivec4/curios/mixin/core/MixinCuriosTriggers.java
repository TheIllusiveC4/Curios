package top.theillusivec4.curios.mixin.core;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosTriggers;
import top.theillusivec4.curios.mixin.CuriosTriggersMixinHooks;

@Mixin(value = CuriosTriggers.class, remap = false)
public class MixinCuriosTriggers {

  @Inject(at = @At("HEAD"), method = "equip", cancellable = true)
  private static void curios$equip(ItemPredicate.Builder itemPredicate,
                                   CallbackInfoReturnable<CriterionTriggerInstance> cir) {
    cir.setReturnValue(CuriosTriggersMixinHooks.equip(itemPredicate));
  }

  @Inject(at = @At("HEAD"), method = "equipAtLocation", cancellable = true)
  private static void curios$equip(ItemPredicate.Builder itemPredicate,
                                   LocationPredicate.Builder locationPredicate,
                                   CallbackInfoReturnable<CriterionTriggerInstance> cir) {
    cir.setReturnValue(CuriosTriggersMixinHooks.equipAtLocation(itemPredicate, locationPredicate));
  }
}
