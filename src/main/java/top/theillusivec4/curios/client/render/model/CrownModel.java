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
    this.crown.func_228301_a_(-3.0F, -12.0F, -3.0F, 6, 4, 6, 0.0F);
  }

  @Override
  public void func_225597_a_(@Nonnull T entity, float limbSwing, float limbSwingAmount,
      float ageInTicks, float netHeadYaw, float netHeadPitch) {

  }

  @Override
  public void func_225598_a_(@Nonnull MatrixStack matrixStack,
      @Nonnull IVertexBuilder vertexBuilder, int light, int overlay, float red, float green,
      float blue, float alpha) {
    this.crown.func_228308_a_(matrixStack, vertexBuilder, light, overlay);
  }
}
