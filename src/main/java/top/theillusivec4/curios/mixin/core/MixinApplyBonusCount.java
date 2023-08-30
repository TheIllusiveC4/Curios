package top.theillusivec4.curios.mixin.core;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.theillusivec4.curios.mixin.CuriosUtilMixinHooks;

@Mixin(ApplyBonusCount.class)
public class MixinApplyBonusCount {

  @Shadow
  @Final
  Enchantment enchantment;

  @ModifyVariable(
      at = @At(
          value = "INVOKE_ASSIGN",
          target = "net/minecraft/world/item/enchantment/EnchantmentHelper.getItemEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I"),
      method = "run")
  private int curios$applyFortune(int enchantmentLevel, ItemStack stack, LootContext lootContext) {

    if (this.enchantment == Enchantments.BLOCK_FORTUNE) {
      return enchantmentLevel + CuriosUtilMixinHooks.getFortuneLevel(lootContext);
    } else {
      return enchantmentLevel;
    }
  }
}
