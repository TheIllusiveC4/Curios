package top.theillusivec4.curios.mixin.core;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.mixin.CuriosUtilMixinHooks;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

  @SuppressWarnings("ConstantConditions")
  @Inject(at = @At("TAIL"), method = "canFreeze()Z", cancellable = true)
  public void curio$canFreeze(CallbackInfoReturnable<Boolean> cir) {

    if (CuriosUtilMixinHooks.isFreezeImmune((LivingEntity) (Object) this)) {
      cir.setReturnValue(false);
    }
  }
}
