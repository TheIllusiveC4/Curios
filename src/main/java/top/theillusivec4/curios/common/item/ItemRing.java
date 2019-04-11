/*
 * Copyright (C) 2018-2019  C4
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.ICurio;
import top.theillusivec4.curios.common.capability.CapCurioItem;

import java.util.UUID;

public class ItemRing extends Item implements ICurio {

    private static final UUID SPEED_UUID = UUID.fromString("8b7c8fcd-89bc-4794-8bb9-eddeb32753a5");
    private static final UUID ARMOR_UUID = UUID.fromString("38faf191-bf78-4654-b349-cc1f4f1143bf");

    public ItemRing() {
        super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).defaultMaxDamage(0));
        this.setRegistryName(Curios.MODID, "ring");
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound unused) {
        return CapCurioItem.createProvider(new ICurio() {

            @Override
            public void onCurioTick(String identifier, EntityLivingBase entityLivingBase) {
                if (!entityLivingBase.getEntityWorld().isRemote && entityLivingBase.ticksExisted % 19 == 0) {
                    entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.HASTE, 20, 0, true, true));
                }
            }

            @Override
            public void playEquipSound(EntityLivingBase entityLivingBase) {
                entityLivingBase.world.playSound(null, entityLivingBase.getPosition(), SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
                        SoundCategory.NEUTRAL, 1.0f, 1.0f);
            }

            @Override
            public Multimap<String, AttributeModifier> getAttributeModifiers(String identifier) {
                Multimap<String, AttributeModifier> atts = HashMultimap.create();

                if (CuriosAPI.getCurioTags(stack.getItem()).contains(identifier)) {
                    atts.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(SPEED_UUID, "Speed bonus", 0.1, 2));
                    atts.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_UUID, "Armor bonus", 2, 0));
                }
                return atts;
            }

            @Override
            public boolean canRightClickEquip() {
                return true;
            }
        });
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
