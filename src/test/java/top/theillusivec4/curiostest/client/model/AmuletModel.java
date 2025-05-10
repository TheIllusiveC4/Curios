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

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public class AmuletModel extends HumanoidModel<HumanoidRenderState> {

  public AmuletModel(ModelPart part) {
    super(part);
    this.setAllVisible(false);
    this.body.visible = true;
  }

  public static LayerDefinition createLayer() {
    MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
    PartDefinition part = mesh.getRoot();
    part.addOrReplaceChild("body",
                           CubeListBuilder.create().texOffs(0, 0)
                               .addBox(-2.0F, 2.0F, -3.0F, 4, 4, 1, CubeDeformation.NONE.extend(0.4F)),
                           PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
    return LayerDefinition.create(mesh, 16, 16);
  }
}
