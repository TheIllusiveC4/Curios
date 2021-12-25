package top.theillusivec4.curios.mixin.core;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.theillusivec4.curios.mixin.CuriosMixinHooks;

@Mixin(ApplyBonusCount.class)
public class MixinApplyBonusCount {

  @ModifyVariable(
      at = @At(
          value = "INVOKE_ASSIGN",
          target = "net/minecraft/world/item/enchantment/EnchantmentHelper.getItemEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I"),
      method = "run")
  private int curios$applyFortune(int fortune, ItemStack stack, LootContext lootContext) {
    return fortune + CuriosMixinHooks.getFortuneLevel(lootContext);
  }
}
