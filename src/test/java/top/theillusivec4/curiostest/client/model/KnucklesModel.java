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

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;

public class KnucklesModel extends HumanoidModel<LivingEntity> {

  public KnucklesModel(ModelPart part) {
    super(part, RenderType::armorCutoutNoCull);
  }

  public static LayerDefinition createLayer() {
    CubeDeformation cube = new CubeDeformation(0.4F);
    MeshDefinition mesh = HumanoidModel.createMesh(cube, 0.0F);
    PartDefinition part = mesh.getRoot();
    part.addOrReplaceChild("right_arm",
        CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, 9.0F, -2.0F, 2, 1, 4, cube),
        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -5.0F, 2.0F, 0.0F));
    part.addOrReplaceChild("left_arm",
        CubeListBuilder.create().mirror().texOffs(0, 0).addBox(1.0F, 9.0F, -2.0F, 2, 1, 4, cube),
        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 5.0F, 2.0F, 0.0F));
    return LayerDefinition.create(mesh, 16, 16);
  }

  @Override
  @Nonnull
  protected Iterable<ModelPart> headParts() {
    return ImmutableList.of(this.head);
  }

  @Override
  @Nonnull
  protected Iterable<ModelPart> bodyParts() {
    return ImmutableList
        .of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat);
  }
}
