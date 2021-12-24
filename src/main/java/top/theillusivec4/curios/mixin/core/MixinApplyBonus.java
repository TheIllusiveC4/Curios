//package top.theillusivec4.curios.mixin.core;
//
//import net.minecraft.item.ItemStack;
//import net.minecraft.loot.LootContext;
//import net.minecraft.loot.LootParameters;
//import net.minecraft.loot.functions.ApplyBonus;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.storage.loot.LootContext;
//import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.ModifyVariable;
//import top.theillusivec4.curios.mixin.CuriosMixinHooks;
//
//@Mixin(ApplyBonusCount.class)
//public class MixinApplyBonus {
//
//  @ModifyVariable(at = @At(value = "INVOKE_ASSIGN", target = "net/minecraft/enchantment/EnchantmentHelper.getEnchantmentLevel(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)I"), method = "doApply")
//  private int curios$applyFortune(int fortune, ItemStack stack, LootContext lootContext) {
//    return fortune +
//        CuriosMixinHooks.getFortuneLevel(lootContext.get(LootParameters.THIS_ENTITY), lootContext);
//  }
//}
