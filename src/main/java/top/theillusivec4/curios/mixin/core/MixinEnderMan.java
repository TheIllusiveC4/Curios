package top.theillusivec4.curios.mixin.core;

import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.mixin.CuriosMixinHooks;

@Mixin(EnderMan.class)
public class MixinEnderMan {

  @SuppressWarnings("ConstantConditions")
  @Inject(at = @At("HEAD"), method = "isLookingAtMe", cancellable = true)
  public void curio$isLookingAtMe(Player player, CallbackInfoReturnable<Boolean> cir) {

    if (CuriosMixinHooks.hasEnderMask(player, (EnderMan) (Object) this)) {
      cir.setReturnValue(false);
    }
  }
}
