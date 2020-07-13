package top.theillusivec4.curios.client.render.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

public class KnucklesModel extends BipedEntityModel<LivingEntity> {

  public KnucklesModel() {
    super(1.0F);
    this.textureWidth = 16;
    this.textureHeight = 16;
    this.rightArm = new ModelPart(this, 0, 0);
    this.rightArm.setPivot(-5.0F, 2.0F, 0.0F);
    this.rightArm.addCuboid(-3.0F, 9.0F, -2.0F, 2, 1, 4, 0.4F);
    this.leftArm = new ModelPart(this, 0, 0);
    this.leftArm.mirror = true;
    this.leftArm.setPivot(5.0F, 2.0F, 0.0F);
    this.leftArm.addCuboid(1.0F, 9.0F, -2.0F, 2, 1, 4, 0.4F);
  }

  @Override
  public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
      float red, float green, float blue, float alpha) {
    this.rightArm.render(matrices, vertices, light, overlay);
    this.leftArm.render(matrices, vertices, light, overlay);
  }
}
