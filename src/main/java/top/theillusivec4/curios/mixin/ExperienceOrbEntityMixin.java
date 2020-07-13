/*
 * Copyright (c) 2018-2020 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.curios.mixin;

import java.util.Map;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {

  @Shadow
  int amount;

  @Inject(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "net/minecraft/enchantment/EnchantmentHelper.chooseEquipmentWith (Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/entity/LivingEntity;Ljava/util/function/Predicate;)Ljava/util/Map$Entry;"))
  private void onPlayerCollision(PlayerEntity playerEntity, CallbackInfo cb) {
    @SuppressWarnings("ConstantConditions") ExperienceOrbEntity orb = (ExperienceOrbEntity) (Object) this;
    CuriosApi.getCuriosHelper().getCuriosHandler(playerEntity).ifPresent(handler -> {
      Map<String, ICurioStacksHandler> curios = handler.getCurios();

      for (ICurioStacksHandler stacksHandler : curios.values()) {

        for (int i = 0; i < stacksHandler.getSlots(); i++) {
          ItemStack stack = stacksHandler.getStacks().getStack(i);

          if (!stack.isEmpty() && EnchantmentHelper.getLevel(Enchantments.MENDING, stack) > 0
              && stack.isDamaged()) {
            playerEntity.experiencePickUpDelay = 2;
            playerEntity.sendPickup(orb, 1);
            int toRepair = Math.min(orb.getExperienceAmount() * 2, stack.getDamage());
            this.amount -= toRepair / 2;
            stack.setDamage(stack.getDamage() - toRepair);

            if (orb.getExperienceAmount() > 0) {
              playerEntity.addExperience(orb.getExperienceAmount());
            }
            orb.remove();
            cb.cancel();
            return;
          }
        }
      }
    });
  }
}
