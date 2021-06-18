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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.common.capability.CurioItemCapability;
import top.theillusivec4.curiostest.CuriosTest;
import top.theillusivec4.curiostest.client.model.KnucklesModel;

public class KnucklesItem extends Item {

  private static final ResourceLocation KNUCKLES_TEXTURE = new ResourceLocation(CuriosTest.MODID,
      "textures/entity/knuckles.png");

  public KnucklesItem() {
    super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1));
    this.setRegistryName(CuriosTest.MODID, "knuckles");
  }

  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT unused) {
    return CurioItemCapability.createProvider(new ICurio() {
      private Object model;

      @Override
      public ItemStack getStack() {
        return stack;
      }

      @Override
      public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext,
                                                                          UUID uuid) {
        Multimap<Attribute, AttributeModifier> atts = HashMultimap.create();
        atts.put(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(uuid, CuriosTest.MODID + ":attack_damage_bonus", 4,
                AttributeModifier.Operation.ADDITION));
        return atts;
      }

      @Override
      public boolean canRender(String identifier, int index, LivingEntity livingEntity) {
        return true;
      }

      @Override
      public void render(String identifier, int index, MatrixStack matrixStack,
                         IRenderTypeBuffer renderTypeBuffer, int light, LivingEntity livingEntity,
                         float limbSwing, float limbSwingAmount, float partialTicks,
                         float ageInTicks, float netHeadYaw, float headPitch) {

        if (!(this.model instanceof KnucklesModel)) {
          this.model = new KnucklesModel();
        }
        KnucklesModel knuckles = (KnucklesModel) this.model;
        knuckles.setLivingAnimations(livingEntity, limbSwing, limbSwingAmount, partialTicks);
        knuckles.setRotationAngles(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
            headPitch);
        ICurio.RenderHelper.followBodyRotations(livingEntity, knuckles);
        IVertexBuilder vertexBuilder = ItemRenderer
            .getBuffer(renderTypeBuffer, knuckles.getRenderType(KNUCKLES_TEXTURE), false,
                stack.hasEffect());
        knuckles
            .render(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F,
                1.0F);
      }
    });
  }

  @Override
  public boolean hasEffect(@Nonnull ItemStack stack) {
    return true;
  }
}
