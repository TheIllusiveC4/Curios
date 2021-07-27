//package top.theillusivec4.curios.mixin.core;
//
//import net.minecraft.entity.LivingEntity;
//import net.minecraft.entity.monster.piglin.PiglinTasks;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//import top.theillusivec4.curios.mixin.CuriosMixinHooks;
//
//@Mixin(PiglinTasks.class)
//public class MixinPiglinTasks {
//
//  @Inject(at = @At("HEAD"), method = "isWearingGold", cancellable = true)
//  private static void curios$isNeutral(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
//
//    if (CuriosMixinHooks.canNeutralizePiglins(livingEntity)) {
//      cir.setReturnValue(true);
//    }
//  }
//}
