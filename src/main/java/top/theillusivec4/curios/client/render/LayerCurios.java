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

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;
import top.theillusivec4.curios.common.CuriosConfig;

import javax.annotation.Nonnull;
import java.util.SortedMap;

public class LayerCurios implements LayerRenderer<EntityLivingBase> {

    @Override
    public void render(@Nonnull EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        if (!CuriosConfig.CLIENT.renderCurios.get()) {
            return;
        }
        GlStateManager.pushMatrix();
        CuriosAPI.getCuriosHandler(entitylivingbaseIn).ifPresent(handler -> {
            SortedMap<String, CurioStackHandler> curios = handler.getCurioMap();

            for (String id : curios.keySet()) {
                CurioStackHandler stackHandler = curios.get(id);

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);

                    if (!stack.isEmpty()) {
                        CuriosAPI.getCurio(stack).ifPresent(curio -> {
                            if (curio.hasRender(id, entitylivingbaseIn)) {
                                GlStateManager.pushMatrix();
                                GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                                curio.doRender(id, entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks,
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
