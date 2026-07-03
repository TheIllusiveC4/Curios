package top.theillusivec4.curios.mixin.core;

import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.TagValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import top.theillusivec4.curios.mixin.CuriosCommonMixinHooks;

@Mixin(EntitySelectorOptions.class)
public class MixinEntitySelectorOptions {

  @Inject(
      at = @At(
          value = "INVOKE",
          target = "net/minecraft/nbt/NbtUtils.compareNbt(Lnet/minecraft/nbt/Tag;"
              + "Lnet/minecraft/nbt/Tag;Z)Z"),
      method = "/^lambda\\$bootStrap\\$\\d/ desc=/^\\(Lnet\\/minecraft\\/nbt\\/CompoundTag;"
          + "ZLnet\\/minecraft\\/world\\/entity\\/Entity;\\)Z/",
      locals = LocalCapture.CAPTURE_FAILSOFT
  )
  private static void curios$nbtmerger(CompoundTag tag, boolean inverted, Entity e,
                                       CallbackInfoReturnable<Boolean> cir,
                                       ProblemReporter.ScopedCollector reporter,
                                       TagValueOutput output) {
    CuriosCommonMixinHooks.mergeCuriosInventory(reporter, output.buildResult(), e);
  }
}
