package top.theillusivec4.curiostest.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curiostest.CuriosTest;
import top.theillusivec4.curiostest.client.CuriosLayerDefinitions;
import top.theillusivec4.curiostest.client.model.CrownModel;

public class CrownRenderer<L extends LivingEntity> implements ICurioRenderer {

  private static final ResourceLocation CROWN_TEXTURE = new ResourceLocation(CuriosTest.MODID,
      "textures/entity/crown.png");
  private final CrownModel<L> model;

  public CrownRenderer() {
    this.model = new CrownModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(CuriosLayerDefinitions.CROWN));
  }

  @Override
  public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack,
                                                                        SlotContext slotContext,
                                                                        PoseStack matrixStack,
                                                                        RenderLayerParent<T, M> renderLayerParent,
                                                                        MultiBufferSource renderTypeBuffer,
                                                                        int light, float limbSwing,
                                                                        float limbSwingAmount,
                                                                        float partialTicks,
                                                                        float ageInTicks,
                                                                        float netHeadYaw,
                                                                        float headPitch) {
    ICurioRenderer.followHeadRotations(slotContext.entity(), this.model.crown);
    VertexConsumer vertexconsumer = ItemRenderer
        .getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(CROWN_TEXTURE), false,
            stack.hasFoil());
    this.model
        .renderToBuffer(matrixStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F,
            1.0F, 1.0F);
  }
}
