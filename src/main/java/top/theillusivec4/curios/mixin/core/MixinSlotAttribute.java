package top.theillusivec4.curios.mixin.core;

import java.util.Map;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.common.CuriosHelper;

// todo: Remove in 1.20.2
@Mixin(value = SlotAttribute.class, remap = false)
public class MixinSlotAttribute {

  @Shadow
  @Final
  private static Map<String, SlotAttribute> SLOT_ATTRIBUTES;

  @Inject(at = @At("HEAD"), method = "getOrCreate", cancellable = true)
  private static void curios$slotAttribute(String id, CallbackInfoReturnable<SlotAttribute> ci) {
    ci.setReturnValue(SLOT_ATTRIBUTES.computeIfAbsent(id, CuriosHelper.SlotAttributeWrapper::new));
  }
}
