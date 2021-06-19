package top.theillusivec4.curiostest.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curiostest.CuriosTest;
import top.theillusivec4.curiostest.client.model.KnucklesModel;

public class KnucklesRenderer implements ICurioRenderer {

  private static final ResourceLocation KNUCKLES_TEXTURE = new ResourceLocation(CuriosTest.MODID,
      "textures/entity/knuckles.png");

  private static final KnucklesModel MODEL = new KnucklesModel();

  @Override
  public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack,
                                                                        SlotContext slotContext,
                                                                        IEntityRenderer<T, M> entityRenderer,
                                                                        MatrixStack matrixStack,
                                                                        IRenderTypeBuffer renderTypeBuffer,
                                                                        int light, float limbSwing,
                                                                        float limbSwingAmount,
                                                                        float partialTicks,
                                                                        float ageInTicks,
                                                                        float netHeadYaw,
                                                                        float headPitch) {
    MODEL.setLivingAnimations(slotContext.getWearer(), limbSwing, limbSwingAmount, partialTicks);
    MODEL.setRotationAngles(slotContext.getWearer(), limbSwing, limbSwingAmount, ageInTicks,
        netHeadYaw, headPitch);
    ICurioRenderer.followBodyRotations(slotContext.getWearer(), MODEL);
    IVertexBuilder vertexBuilder = ItemRenderer
        .getBuffer(renderTypeBuffer, MODEL.getRenderType(KNUCKLES_TEXTURE), false,
            stack.hasEffect());
    MODEL.render(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F,
        1.0F);
  }
}
