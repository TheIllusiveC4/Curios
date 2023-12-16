package top.theillusivec4.curios.platform.services;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ICuriosPlatform {

  boolean makesPiglinsNeutral(ItemStack stack, LivingEntity livingEntity);

  boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity livingEntity);

  boolean isEnderMask(ItemStack stack, Player player, EnderMan enderMan);
}
