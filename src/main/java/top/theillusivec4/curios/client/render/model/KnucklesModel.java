package top.theillusivec4.curios.client.render.model;

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
    this.bipedRightArm.func_228301_a_(-3.0F, 9.0F, -2.0F, 2, 1, 4, 0.4F);
    this.bipedLeftArm = new ModelRenderer(this, 0, 0);
    this.bipedLeftArm.mirror = true;
    this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
    this.bipedLeftArm.func_228301_a_(1.0F, 9.0F, -2.0F, 2, 1, 4, 0.4F);
  }

  @Override
  public void func_225598_a_(@Nonnull MatrixStack matrixStack,
      @Nonnull IVertexBuilder vertexBuilder, int light, int overlay, float red, float green,
      float blue, float alpha) {
    this.bipedRightArm.func_228308_a_(matrixStack, vertexBuilder, light, overlay);
    this.bipedLeftArm.func_228308_a_(matrixStack, vertexBuilder, light, overlay);
  }
}
