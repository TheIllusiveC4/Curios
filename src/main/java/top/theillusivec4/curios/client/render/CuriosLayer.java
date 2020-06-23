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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CuriosLayer<T extends LivingEntity, M extends EntityModel<T>> extends
    LayerRenderer<T, M> {

  public CuriosLayer(IEntityRenderer<T, M> renderer) {
    super(renderer);
  }

  @Override
  public void render(@Nonnull MatrixStack matrixStack, @Nonnull IRenderTypeBuffer renderTypeBuffer,
      int light, @Nonnull T livingEntity, float limbSwing, float limbSwingAmount,
      float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
    matrixStack.push();
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity)
        .ifPresent(handler -> handler.getCurios().forEach((id, stacksHandler) -> {
          IDynamicStackHandler stackHandler = stacksHandler.getStacks();
          IDynamicStackHandler cosmeticStacksHandler = stacksHandler.getCosmeticStacks();

          for (int i = 0; i < stackHandler.getSlots(); i++) {
            ItemStack stack = cosmeticStacksHandler.getStackInSlot(i);

            if (stack.isEmpty()) {
              stack = stackHandler.getStackInSlot(i);
            }

            if (!stack.isEmpty() && stacksHandler.getRenders().get(i)) {
              int index = i;

              CuriosApi.getCuriosHelper().getCurio(stack).ifPresent(curio -> {

                if (curio.canRender(id, index, livingEntity)) {
                  matrixStack.push();
                  RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                  curio.render(id, index, matrixStack, renderTypeBuffer, light, livingEntity,
                      limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
                  matrixStack.pop();
                }
              });
            }
          }
        }));
    matrixStack.pop();
  }
}
