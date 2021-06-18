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

package top.theillusivec4.curiostest.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class KnucklesModel extends BipedModel<LivingEntity> {

  public KnucklesModel() {
    super(1.0F);
    this.textureWidth = 16;
    this.textureHeight = 16;
    this.bipedRightArm = new ModelRenderer(this, 0, 0);
    this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
    this.bipedRightArm.addBox(-3.0F, 9.0F, -2.0F, 2, 1, 4, 0.4F);
    this.bipedLeftArm = new ModelRenderer(this, 0, 0);
    this.bipedLeftArm.mirror = true;
    this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
    this.bipedLeftArm.addBox(1.0F, 9.0F, -2.0F, 2, 1, 4, 0.4F);
  }

  @Override
  public void render(@Nonnull MatrixStack matrixStack, @Nonnull IVertexBuilder vertexBuilder,
                     int light, int overlay, float red, float green, float blue, float alpha) {
    this.bipedRightArm.render(matrixStack, vertexBuilder, light, overlay);
    this.bipedLeftArm.render(matrixStack, vertexBuilder, light, overlay);
  }
}
