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

package top.theillusivec4.curios.client.render.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class CrownModel<T extends LivingEntity> extends EntityModel<T> {

  public ModelRenderer crown;

  public CrownModel() {
    this.textureWidth = 32;
    this.textureHeight = 32;
    this.crown = new ModelRenderer(this, 0, 0);
    this.crown.setRotationPoint(0.0F, 0.0F, 0.0F);
    this.crown.addBox(-3.0F, -12.0F, -3.0F, 6, 4, 6, 0.0F);
  }

  @Override
  public void setRotationAngles(@Nonnull T entity, float limbSwing, float limbSwingAmount,
                                float ageInTicks, float netHeadYaw, float netHeadPitch) {

  }

  @Override
  public void render(@Nonnull MatrixStack matrixStack, @Nonnull IVertexBuilder vertexBuilder,
                     int light, int overlay, float red, float green, float blue, float alpha) {
    this.crown.render(matrixStack, vertexBuilder, light, overlay);
  }
}
