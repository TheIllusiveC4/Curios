package top.theillusivec4.curios.mixin.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import top.theillusivec4.curios.mixin.CuriosMixinHooks;

@Mixin(ShearsItem.class)
public class MixinShearsItem {

  @ModifyArg(
      at = @At(
          value = "INVOKE",
          target = "net/minecraftforge/common/IForgeShearable.onSheared(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;I)Ljava/util/List;"),
      method = "interactLivingEntity")
  private int curios$applyFortuneToShears(Player player, ItemStack stack, Level level, BlockPos pos,
                                          int fortune) {
    return fortune + CuriosMixinHooks.getFortuneLevel(player);
  }
}
