package top.theillusivec4.curios.platform;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.platform.services.ICuriosPlatform;

public class NeoForgeCurios implements ICuriosPlatform {

  @Override
  public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity livingEntity) {
    return stack.makesPiglinsNeutral(livingEntity);
  }

  @Override
  public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity livingEntity) {
    return stack.canWalkOnPowderedSnow(livingEntity);
  }

  @Override
  public boolean isEnderMask(ItemStack stack, Player player, EnderMan enderMan) {
    return stack.isEnderMask(player, enderMan);
  }
}
