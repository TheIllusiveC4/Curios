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

package top.theillusivec4.curiostest.common.item;

import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.common.capability.CurioItemCapability;

public class CrownItem extends Item {

  public CrownItem() {
    super(new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1).defaultDurability(2000));
  }

  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag unused) {
    return CurioItemCapability.createProvider(new ICurio() {

      @Override
      public ItemStack getStack() {
        return stack;
      }

      @Override
      public void curioTick(SlotContext slotContext) {
        LivingEntity livingEntity = slotContext.entity();

        if (!livingEntity.level.isClientSide() && livingEntity.tickCount % 20 == 0) {
          livingEntity
              .addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, -1, true, true));
          stack.hurtAndBreak(1, livingEntity,
              damager -> CuriosApi.getCuriosHelper().onBrokenCurio(slotContext));
        }
      }
    });
  }

  @Override
  public boolean isFoil(@Nonnull ItemStack stack) {
    return true;
  }
}
