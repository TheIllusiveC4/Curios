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

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

public class AmuletModel<T extends LivingEntity> extends EntityModel<T> {

  public ModelPart amulet;

  public AmuletModel() {
    this.textureWidth = 16;
    this.textureHeight = 16;
    this.amulet = new ModelPart(this, 0, 0);
    this.amulet.setPivot(0.0F, 0.0F, 0.0F);
    this.amulet.addCuboid(-2.0F, 2.0F, -3.0F, 4, 4, 1, 0.0F);
  }

  @Override
  public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress,
      float headYaw, float headPitch) {

  }

  @Override
  public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
      float red, float green, float blue, float alpha) {
    this.amulet.render(matrices, vertices, light, overlay);
  }
}
