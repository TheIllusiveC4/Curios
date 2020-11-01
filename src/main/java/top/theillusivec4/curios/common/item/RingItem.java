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

import com.google.common.collect.HashMultimap;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.common.capability.CurioItemCapability;

public class RingItem extends Item {

  private static final UUID SPEED_UUID = UUID.fromString("8b7c8fcd-89bc-4794-8bb9-eddeb32753a5");
  private static final UUID ARMOR_UUID = UUID.fromString("38faf191-bf78-4654-b349-cc1f4f1143bf");

  public RingItem() {
	super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).defaultMaxDamage(0));
	this.setRegistryName(Curios.MODID, "ring");
  }

  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT unused) {
	return CurioItemCapability.createProvider(new ICurio() {

	  @Override
	  public void curioTick(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {

		if (!livingEntity.getEntityWorld().isRemote && livingEntity.ticksExisted % 19 == 0) {
		  livingEntity.addPotionEffect(new EffectInstance(Effects.HASTE, 20, 0, true, true));
		}
	  }

	  @Override
	  public void playRightClickEquipSound(String identifer, LivingEntity livingEntity, ItemStack stack) {
		livingEntity.world.playSound(null, new BlockPos(livingEntity.getPositionVec()),
			SoundEvents.ITEM_ARMOR_EQUIP_GOLD, SoundCategory.NEUTRAL, 1.0f, 1.0f);
	  }

	  @Override
	  public Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> atts = HashMultimap.create();

		if (CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).contains(identifier)) {
		  atts.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(SPEED_UUID, "Speed bonus", 0.1,
			  AttributeModifier.Operation.MULTIPLY_TOTAL));
		  atts.put(Attributes.ARMOR, new AttributeModifier(ARMOR_UUID, "Armor bonus", 2,
			  AttributeModifier.Operation.ADDITION));
		}
		return atts;
	  }

	  @Nonnull
	  @Override
	  public DropRule getDropRule(String identifier, LivingEntity livingEntity, ItemStack stack) {
		return DropRule.ALWAYS_KEEP;
	  }

	  @Override
	  public boolean canRightClickEquip(LivingEntity livingEntity, ItemStack stack) {
		return true;
	  }
	});
  }

  @Override
  public boolean hasEffect(ItemStack stack) {
	return true;
  }
}
