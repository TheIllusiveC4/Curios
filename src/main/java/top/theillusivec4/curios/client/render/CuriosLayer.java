/*
 * Copyright (C) 2018-2019  C4
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

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;
import top.theillusivec4.curios.common.CuriosConfig;

public class CuriosLayer<T extends LivingEntity, M extends EntityModel<T>> extends
    LayerRenderer<T, M> {

  public CuriosLayer(IEntityRenderer<T, M> renderer) {
    super(renderer);
  }

  @Override
  public void render(@Nonnull LivingEntity livingEntity, float limbSwing, float limbSwingAmount,
      float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

    if (!CuriosConfig.CLIENT.renderCurios.get()) {
      return;
    }
    GlStateManager.pushMatrix();
    CuriosAPI.getCuriosHandler(livingEntity).ifPresent(handler -> {
      SortedMap<String, CurioStackHandler> curios = handler.getCurioMap();

      if (livingEntity.shouldRenderSneaking()) {
        GlStateManager.translatef(0.0f, 0.2f, 0.0f);
      }

      for (String id : curios.keySet()) {
        CurioStackHandler stackHandler = curios.get(id);

        for (int i = 0; i < stackHandler.getSlots(); i++) {
          ItemStack stack = stackHandler.getStackInSlot(i);

          if (!stack.isEmpty()) {
            CuriosAPI.getCurio(stack).ifPresent(curio -> {
              if (curio.hasRender(id, livingEntity)) {
                GlStateManager.pushMatrix();
                GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                curio.doRender(id, livingEntity, limbSwing, limbSwingAmount, partialTicks,
                    ageInTicks, netHeadYaw, headPitch, scale);
                GlStateManager.popMatrix();
              }
            });
          }
        }
      }
    });
    GlStateManager.popMatrix();
  }

  @Override
  public boolean shouldCombineTextures() {

    return false;
  }
}
