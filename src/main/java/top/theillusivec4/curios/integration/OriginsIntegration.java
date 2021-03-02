package top.theillusivec4.curios.integration;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ElytraFlightPower;
import io.github.apace100.origins.power.RestrictArmorPower;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class OriginsIntegration {

  public static boolean canEquip(ItemStack stack, PlayerEntity player) {
    OriginComponent component = ModComponents.ORIGIN.get(player);
    EquipmentSlot slot = MobEntity.getPreferredEquipmentSlot(stack);

    if (component.getPowers(RestrictArmorPower.class).stream()
        .anyMatch(rap -> !rap.canEquip(stack, slot))) {
      return false;
    }

    if (OriginComponent.getPowers(player, ElytraFlightPower.class).size() > 0) {
      return stack.getItem() != Items.ELYTRA;
    }
    return true;
  }
}
