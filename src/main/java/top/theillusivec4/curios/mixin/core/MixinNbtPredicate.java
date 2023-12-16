package top.theillusivec4.curios.mixin.core;

import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.theillusivec4.curios.mixin.CuriosUtilMixinHooks;

@Mixin(NbtPredicate.class)
public class MixinNbtPredicate {

  @ModifyVariable(
      at = @At("RETURN"),
      method = "getEntityTagToCompare"
  )
  private static CompoundTag curios$mergeCuriosInventory(CompoundTag compoundTag, Entity entity) {
    return CuriosUtilMixinHooks.mergeCuriosInventory(compoundTag, entity);
  }
}
