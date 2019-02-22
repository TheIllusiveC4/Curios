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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.capability.ICurio;
import top.theillusivec4.curios.common.capability.CapCurioItem;

public class ItemAmulet extends Item implements ICurio {

    public ItemAmulet() {
        super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).defaultMaxDamage(0));
        this.setRegistryName(Curios.MODID, "amulet");
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound unused) {
        return CapCurioItem.createProvider(new ICurio() {

            @Override
            public void onCurioTick(ItemStack stack, String identifier, EntityLivingBase entityLivingBase) {

                if (!entityLivingBase.getEntityWorld().isRemote && entityLivingBase.ticksExisted % 40 == 0) {
                    entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 80, 0, true, true));
                }
            }
        });
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
