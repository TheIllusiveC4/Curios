package top.theillusivec4.curios.client.render;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CuriosFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends
    FeatureRenderer<T, M> {

  public CuriosFeatureRenderer(FeatureRendererContext<T, M> context) {
    super(context);
  }

  public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
      T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress,
      float headYaw, float headPitch) {
    matrices.push();
    CuriosApi.getCuriosHelper().getCuriosHandler(entity)
        .ifPresent(handler -> handler.getCurios().forEach((id, stacksHandler) -> {
          IDynamicStackHandler stackHandler = stacksHandler.getStacks();
          IDynamicStackHandler cosmeticStacksHandler = stacksHandler.getCosmeticStacks();

          for (int i = 0; i < stackHandler.size(); i++) {
            ItemStack stack = cosmeticStacksHandler.getStack(i);

            if (stack.isEmpty() && stacksHandler.getRenders().get(i)) {
              stack = stackHandler.getStack(i);
            }

            if (!stack.isEmpty()) {
              int index = i;

              CuriosApi.getCuriosHelper().getRenderableCurio(stack).ifPresent(curio -> {
                matrices.push();
                curio.render(id, index, matrices, vertexConsumers, light, entity, limbAngle,
                    limbDistance, tickDelta, animationProgress, headYaw, headPitch);
                matrices.pop();
              });
            }
          }
        }));
    matrices.pop();
  }
}
