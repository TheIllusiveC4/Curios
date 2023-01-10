package top.theillusivec4.curios.mixin.core;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.mixin.CuriosMixinHooks;

@Mixin(PowderSnowBlock.class)
public class MixinPowderSnowBlock {

  @Inject(at = @At("RETURN"), method = "canEntityWalkOnPowderSnow", cancellable = true)
  private static void curios$canEntityWalkOnPowderSnow(Entity entity,
                                                       CallbackInfoReturnable<Boolean> cir) {

    if (entity instanceof LivingEntity livingEntity && CuriosMixinHooks.canWalkOnPowderSnow(livingEntity)) {
      cir.setReturnValue(true);
    }
  }
}
