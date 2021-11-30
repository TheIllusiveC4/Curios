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

package top.theillusivec4.curios.common.item;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.common.capability.CurioItemCapability;

public class RingItem extends Item {

  public RingItem() {
    super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).defaultMaxDamage(0));
    this.setRegistryName(Curios.MODID, "ring");
  }

  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT unused) {
    return CurioItemCapability.createProvider(new ICurio() {

      @Override
      public void curioTick(String identifier, int index, LivingEntity livingEntity) {

        if (!livingEntity.getEntityWorld().isRemote && livingEntity.ticksExisted % 19 == 0) {
          livingEntity.addPotionEffect(new EffectInstance(Effects.HASTE, 20, 0, true, true));
        }
      }

      @Override
      public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext,
                                                                          UUID uuid) {
        Multimap<Attribute, AttributeModifier> atts = LinkedHashMultimap.create();
        atts.put(Attributes.MOVEMENT_SPEED,
            new AttributeModifier(uuid, Curios.MODID + ":speed_bonus", 0.1,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        atts.put(Attributes.ARMOR,
            new AttributeModifier(uuid, Curios.MODID + ":armor_bonus", 2,
                AttributeModifier.Operation.ADDITION));
        return atts;
      }

      @Nonnull
      @Override
      public DropRule getDropRule(LivingEntity livingEntity) {
        return DropRule.ALWAYS_KEEP;
      }

      @Nonnull
      @Override
      public SoundInfo getEquipSound(SlotContext slotContext) {
        return new SoundInfo(SoundEvents.ITEM_ARMOR_EQUIP_GOLD, 1.0f, 1.0f);
      }

      @Override
      public boolean canEquipFromUse(SlotContext slot) {
        return true;
      }
    });
  }

  @Override
  public boolean hasEffect(@Nonnull ItemStack stack) {
    return true;
  }
}
