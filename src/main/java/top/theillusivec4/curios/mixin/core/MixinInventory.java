package top.theillusivec4.curios.mixin.core;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.mixin.CuriosUtilMixinHooks;

@Mixin(value = Inventory.class, priority = 4)
public abstract class MixinInventory implements Container {

  @Shadow
  @Final
  public Player player;

  @Inject(
      at = @At("TAIL"),
      method = "contains(Lnet/minecraft/world/item/ItemStack;)Z",
      cancellable = true
  )
  private void curios$containsStack(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {

    if (CuriosUtilMixinHooks.containsStack(this.player, stack)) {
      cir.setReturnValue(true);
    }
  }

  @Inject(
      at = @At("TAIL"),
      method = "contains(Lnet/minecraft/tags/TagKey;)Z",
      cancellable = true
  )
  private void curios$containsTag(TagKey<Item> tagKey, CallbackInfoReturnable<Boolean> cir) {

    if (CuriosUtilMixinHooks.containsTag(this.player, tagKey)) {
      cir.setReturnValue(true);
    }
  }

  @Override
  public boolean hasAnyMatching(@Nonnull Predicate<ItemStack> predicate) {
    return Container.super.hasAnyMatching(predicate);
  }

  @Inject(
      at = @At("TAIL"),
      method = "hasAnyMatching(Ljava/util/function/Predicate;)Z",
      cancellable = true
  )
  private void curios$hasAnyMatching(Predicate<ItemStack> predicate,
                                     CallbackInfoReturnable<Boolean> cir) {

    if (!cir.getReturnValue() && CuriosUtilMixinHooks.hasAnyMatching(this.player, predicate)) {
      cir.setReturnValue(true);
    }
  }
}
