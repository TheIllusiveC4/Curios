package top.theillusivec4.curios.client.render.model;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.LivingEntity;

public class KnucklesModel extends BipedModel<LivingEntity> {

  public KnucklesModel() {

    this.textureWidth = 16;
    this.textureHeight = 16;
    this.bipedRightArm = new RendererModel(this, 0, 0);
    this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
    this.bipedRightArm.addBox(-3.0F, 9.0F, -2.0F, 2, 1, 4, 0.4F);
    this.bipedLeftArm = new RendererModel(this, 0, 0);
    this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
    this.bipedLeftArm.addBox(1.0F, 9.0F, -2.0F, 2, 1, 4, 0.4F);
    this.bipedLeftArm.mirror = true;
  }

  @Override
  public void render(LivingEntity entityIn, float limbSwing, float limbSwingAmount,
      float ageInTicks, float netHeadYaw, float headPitch, float scale) {
    this.bipedRightArm.render(scale);
    this.bipedLeftArm.render(scale);
  }
}
