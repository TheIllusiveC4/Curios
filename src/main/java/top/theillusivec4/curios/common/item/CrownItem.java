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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.client.render.model.CrownModel;
import top.theillusivec4.curios.common.capability.CurioItemCapability;

public class CrownItem extends Item {

  private static final ResourceLocation CROWN_TEXTURE = new ResourceLocation(Curios.MODID,
	  "textures/entity/crown.png");

  public CrownItem() {
	super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).defaultMaxDamage(2000));
	this.setRegistryName(Curios.MODID, "crown");
  }

  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT unused) {
	return CurioItemCapability.createProvider(new ICurio() {
	  private Object model;

	  @Override
	  public void curioTick(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {

		if (!livingEntity.getEntityWorld().isRemote && livingEntity.ticksExisted % 20 == 0) {
		  livingEntity
		  .addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, 300, -1, true, true));
		  stack.damageItem(1, livingEntity,
			  damager -> CuriosApi.getCuriosHelper().onBrokenCurio(identifier, index, damager));
		}
	  }

	  @Override
	  public boolean canRender(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {
		return true;
	  }

	  @Override
	  public void render(String identifier, int index, MatrixStack matrixStack,
		  IRenderTypeBuffer renderTypeBuffer, int light, LivingEntity livingEntity, float limbSwing,
		  float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
		  float headPitch, ItemStack stack) {

		if (!(this.model instanceof CrownModel)) {
		  this.model = new CrownModel<>();
		}
		CrownModel<?> crown = (CrownModel<?>) this.model;
		ICurio.RenderHelper.followHeadRotations(livingEntity, crown.crown);
		IVertexBuilder vertexBuilder = ItemRenderer
			.getBuffer(renderTypeBuffer, crown.getRenderType(CROWN_TEXTURE), false,
				stack.hasEffect());
		crown.render(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F,
			1.0F);
	  }
	});
  }

  @Override
  public boolean hasEffect(ItemStack stack) {
	return true;
  }
}
