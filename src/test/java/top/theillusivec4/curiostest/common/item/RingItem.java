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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.common.capability.CurioItemCapability;
import top.theillusivec4.curiostest.CuriosTest;

public class RingItem extends Item implements Wearable {

  public RingItem() {
    super(new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1).defaultDurability(0));
  }

  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag unused) {
    return CurioItemCapability.createProvider(new ICurio() {

      @Override
      public void curioTick(SlotContext slotContext) {
        LivingEntity livingEntity = slotContext.entity();

        if (!livingEntity.level.isClientSide() && livingEntity.tickCount % 19 == 0) {
          livingEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20, 0, true, true));
        }
      }

      @Override
      public ItemStack getStack() {
        return stack;
      }

      @Override
      public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext,
                                                                          UUID uuid) {
        Multimap<Attribute, AttributeModifier> atts = LinkedHashMultimap.create();
        atts.put(Attributes.MOVEMENT_SPEED,
            new AttributeModifier(uuid, CuriosTest.MODID + ":speed_bonus", 0.1,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        atts.put(Attributes.ARMOR,
            new AttributeModifier(uuid, CuriosTest.MODID + ":armor_bonus", 2,
                AttributeModifier.Operation.ADDITION));
        atts.put(Attributes.KNOCKBACK_RESISTANCE,
            new AttributeModifier(uuid, CuriosTest.MODID + ":knockback_resist", 0.2,
                AttributeModifier.Operation.ADDITION));
        CuriosApi.getCuriosHelper()
            .addSlotModifier(atts, "ring", uuid, 1, AttributeModifier.Operation.ADDITION);
        return atts;
      }

      @Nonnull
      @Override
      public DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel,
                                  boolean recentlyHit) {
        return DropRule.ALWAYS_KEEP;
      }

      @Nonnull
      @Override
      public SoundInfo getEquipSound(SlotContext slotContext) {
        return new SoundInfo(SoundEvents.ARMOR_EQUIP_GOLD, 1.0f, 1.0f);
      }

      @Override
      public boolean canEquipFromUse(SlotContext slot) {
        return true;
      }

      @Override
      public boolean makesPiglinsNeutral(SlotContext slotContext) {
        return true;
      }

      @Override
      public boolean isEnderMask(SlotContext slotContext, EnderMan enderMan) {
        return true;
      }

      @Override
      public int getFortuneLevel(SlotContext slotContext, @Nullable LootContext lootContext) {
        return 3;
      }
    });
  }

  @Override
  public boolean isFoil(@Nonnull ItemStack stack) {
    return true;
  }
}
