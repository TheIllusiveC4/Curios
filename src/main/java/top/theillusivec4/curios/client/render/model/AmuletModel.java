package top.theillusivec4.curios.client.render.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class AmuletModel<T extends LivingEntity> extends EntityModel<T> {

  public ModelRenderer amulet;

  public AmuletModel() {
    this.textureWidth = 16;
    this.textureHeight = 16;
    this.amulet = new ModelRenderer(this, 0, 0);
    this.amulet.setRotationPoint(0.0F, 0.0F, 0.0F);
    this.amulet.addBox(-2.0F, 2.0F, -3.0F, 4, 4, 1, 0.0F);
  }

  @Override
  public void render(@Nonnull T entity, float limbSwing, float limbSwingAmount,
      float ageInTicks, float netHeadYaw, float netHeadPitch) {

  }

  @Override
  public void render(@Nonnull MatrixStack matrixStack,
      @Nonnull IVertexBuilder vertexBuilder, int light, int overlay, float red, float green,
      float blue, float alpha) {
    this.amulet.render(matrixStack, vertexBuilder, light, overlay);
  }
}
