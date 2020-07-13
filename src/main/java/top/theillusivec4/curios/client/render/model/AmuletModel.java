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
