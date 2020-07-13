package top.theillusivec4.curios.client.render;

import nerdhub.cardinal.components.api.event.ItemComponentCallbackV2;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosComponent;
import top.theillusivec4.curios.api.type.component.IRenderableCurio;
import top.theillusivec4.curios.client.render.model.AmuletModel;
import top.theillusivec4.curios.common.CuriosRegistry;

public class CuriosRenderComponents {

  private static final Identifier AMULET_TEXTURE = new Identifier(CuriosApi.MODID,
      "textures/entity/amulet.png");

  public static void register() {
    ItemComponentCallbackV2.event(CuriosRegistry.AMULET).register(
        ((item, itemStack, componentContainer) -> componentContainer
            .put(CuriosComponent.ITEM_RENDER, new IRenderableCurio() {
              AmuletModel<LivingEntity> model = new AmuletModel<>();

              @Override
              public void render(String identifier, int index, MatrixStack matrixStack,
                  VertexConsumerProvider vertexConsumerProvider, int light,
                  LivingEntity livingEntity, float limbSwing, float limbSwingAmount,
                  float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                IRenderableCurio.RenderHelper.translateIfSneaking(matrixStack, livingEntity);
                IRenderableCurio.RenderHelper.rotateIfSneaking(matrixStack, livingEntity);
                VertexConsumer consumer = ItemRenderer
                    .getArmorVertexConsumer(vertexConsumerProvider, model.getLayer(AMULET_TEXTURE),
                        false, itemStack.hasGlint());
                model.render(matrixStack, consumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F,
                    1.0F, 1.0F);
              }
            })));
  }
}
