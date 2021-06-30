package top.theillusivec4.curios.mixin.core;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import top.theillusivec4.curios.mixin.CuriosMixinHooks;

@Mixin(ShearsItem.class)
public class MixinShearsItem {

  @ModifyArg(at = @At(value = "INVOKE", target = "net/minecraftforge/common/IForgeShearable.onSheared(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;I)Ljava/util/List;"), method = "itemInteractionForEntity")
  private int curios$applyFortuneToShears(PlayerEntity player, ItemStack stack, World world,
                                          BlockPos pos, int fortune) {
    return fortune + CuriosMixinHooks.getFortuneLevel(player, null);
  }
}
