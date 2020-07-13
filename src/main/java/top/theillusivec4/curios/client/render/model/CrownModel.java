package top.theillusivec4.curios.client.render.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

public class CrownModel<T extends LivingEntity> extends EntityModel<T> {

  public ModelPart crown;

  public CrownModel() {
    this.textureWidth = 16;
    this.textureHeight = 16;
    this.crown = new ModelPart(this, 0, 0);
    this.crown.setPivot(0.0F, 0.0F, 0.0F);
    this.crown.addCuboid(-3.0F, -12.0F, -3.0F, 6, 4, 6, 0.0F);
  }

  @Override
  public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress,
      float headYaw, float headPitch) {

  }

  @Override
  public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
      float red, float green, float blue, float alpha) {
    this.crown.render(matrices, vertices, light, overlay);
  }
}
