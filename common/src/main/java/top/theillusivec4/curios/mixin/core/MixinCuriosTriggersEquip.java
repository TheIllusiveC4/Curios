package top.theillusivec4.curios.mixin.core;

import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosTriggers;
import top.theillusivec4.curios.api.SlotPredicate;
import top.theillusivec4.curios.common.util.EquipCurioTrigger;

@Mixin(value = CuriosTriggers.EquipBuilder.class, remap = false)
public class MixinCuriosTriggersEquip {

  @Shadow
  private ItemPredicate.Builder itemPredicate;
  @Shadow
  private LocationPredicate.Builder locationPredicate;
  @Shadow
  private SlotPredicate.Builder slotPredicate;

  @Inject(at = @At("HEAD"), method = "build", cancellable = true)
  private void curios$equipAtLocation(
      CallbackInfoReturnable<Criterion<? extends CriterionTriggerInstance>> cir) {
    cir.setReturnValue(EquipCurioTrigger.INSTANCE.createCriterion(
        new EquipCurioTrigger.TriggerInstance(Optional.empty(),
            Optional.of(this.itemPredicate.build()), Optional.of(this.locationPredicate.build()),
            Optional.of(this.slotPredicate.build()))));
  }
}
