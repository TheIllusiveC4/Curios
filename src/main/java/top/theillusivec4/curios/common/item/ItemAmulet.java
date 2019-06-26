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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.capability.ICurio;
import top.theillusivec4.curios.client.render.ModelAmulet;
import top.theillusivec4.curios.common.capability.CapCurioItem;

public class ItemAmulet extends Item implements ICurio {

    public ItemAmulet() {
        super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).defaultMaxDamage(0));
        this.setRegistryName(Curios.MODID, "amulet");
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT unused) {
        return CapCurioItem.createProvider(new ICurio() {

            private final ResourceLocation AMULET_TEXTURE = new ResourceLocation(Curios.MODID, "textures/entity/amulet.png");

            private Object model;

            @Override
            public void onCurioTick(String identifier, LivingEntity entityLivingBase) {

                if (!entityLivingBase.getEntityWorld().isRemote && entityLivingBase.ticksExisted % 40 == 0) {
                    entityLivingBase.addPotionEffect(new EffectInstance(Effects.REGENERATION, 80, 0, true, true));
                }
            }

            @Override
            public boolean hasRender(String identifier, LivingEntity entityLivingBase) {
                return true;
            }

            @Override
            public void doRender(String identifier, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
                Minecraft.getInstance().getTextureManager().bindTexture(AMULET_TEXTURE);
                ICurio.RenderHelper.rotateIfSneaking(entitylivingbaseIn);

                if (!(this.model instanceof ModelAmulet)) {
                    this.model = new ModelAmulet();
                }
                ((ModelAmulet) model).func_78088_a(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            }
        });
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
