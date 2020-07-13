/*
 * Copyright (c) 2018-2020 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

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
