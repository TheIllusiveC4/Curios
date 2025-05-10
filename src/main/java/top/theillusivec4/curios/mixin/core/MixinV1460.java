package top.theillusivec4.curios.mixin.core;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.schemas.V1460;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.mixin.CuriosCommonMixinHooks;

@Mixin(V1460.class)
public class MixinV1460 {

  @Unique
  private static Schema curios$schema;

  @Inject(method = "registerTypes", at = @At("HEAD"))
  private void captureSchema(Schema schemax, Map<String, Supplier<TypeTemplate>> entityTypes,
                             Map<String, Supplier<TypeTemplate>> blockEntityTypes,
                             CallbackInfo ci) {
    curios$schema = schemax;
  }

  @ModifyArg(
      method = {
          "lambda$registerTypes$30"
      },
      at = @At(
          value = "INVOKE",
          target = "com/mojang/datafixers/DSL.optionalFields([Lcom/mojang/datafixers/util/Pair;)Lcom/mojang/datafixers/types/templates/TypeTemplate;")
  )
  private static Pair<String, TypeTemplate>[] curios$attachCuriosFixer(
      Pair<String, TypeTemplate>[] original) {
    return CuriosCommonMixinHooks.attachDataFixer(curios$schema, original);
  }
}
