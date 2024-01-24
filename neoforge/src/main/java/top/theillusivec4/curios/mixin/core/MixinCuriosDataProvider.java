package top.theillusivec4.curios.mixin.core;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosDataProvider;
import top.theillusivec4.curios.api.type.data.IEntitiesData;
import top.theillusivec4.curios.api.type.data.ISlotData;
import top.theillusivec4.curios.common.data.EntitiesData;
import top.theillusivec4.curios.common.data.SlotData;

@Mixin(value = CuriosDataProvider.class, remap = false)
public class MixinCuriosDataProvider {

  @Inject(at = @At("HEAD"), method = "createSlotData", cancellable = true)
  private static void curios$createSlotData(CallbackInfoReturnable<ISlotData> cir) {
    cir.setReturnValue(new SlotData());
  }

  @Inject(at = @At("HEAD"), method = "createEntitiesData", cancellable = true)
  private static void curios$createEntitiesData(CallbackInfoReturnable<IEntitiesData> cir) {
    cir.setReturnValue(new EntitiesData());
  }
}
