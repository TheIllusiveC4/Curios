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
import top.theillusivec4.curios.client.render.model.CrownModel;
import top.theillusivec4.curios.client.render.model.KnucklesModel;
import top.theillusivec4.curios.common.CuriosRegistry;

public class CuriosRenderComponents {

  private static final Identifier AMULET_TEXTURE = new Identifier(CuriosApi.MODID,
      "textures/entity/amulet.png");
  private static final Identifier CROWN_TEXTURE = new Identifier(CuriosApi.MODID,
      "textures/entity/crown.png");
  private static final Identifier KNUCKLES_TEXTURE = new Identifier(CuriosApi.MODID,
      "textures/entity/knuckles.png");

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
                    .getItemGlintConsumer(vertexConsumerProvider, model.getLayer(AMULET_TEXTURE),
                        false, itemStack.hasGlint());
                model.render(matrixStack, consumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F,
                    1.0F, 1.0F);
              }
            })));

    ItemComponentCallbackV2.event(CuriosRegistry.CROWN).register(
        ((item, itemStack, componentContainer) -> componentContainer
            .put(CuriosComponent.ITEM_RENDER, new IRenderableCurio() {
              CrownModel<LivingEntity> model = new CrownModel<>();

              @Override
              public void render(String identifier, int index, MatrixStack matrixStack,
                  VertexConsumerProvider vertexConsumerProvider, int light,
                  LivingEntity livingEntity, float limbSwing, float limbSwingAmount,
                  float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                IRenderableCurio.RenderHelper.followHeadRotations(livingEntity, model.crown);
                VertexConsumer consumer = ItemRenderer
                    .getItemGlintConsumer(vertexConsumerProvider, model.getLayer(CROWN_TEXTURE),
                        false, itemStack.hasGlint());
                model.render(matrixStack, consumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F,
                    1.0F, 1.0F);
              }
            })));

    ItemComponentCallbackV2.event(CuriosRegistry.KNUCKLES).register(
        ((item, itemStack, componentContainer) -> componentContainer
            .put(CuriosComponent.ITEM_RENDER, new IRenderableCurio() {
              KnucklesModel model = new KnucklesModel();

              @Override
              public void render(String identifier, int index, MatrixStack matrixStack,
                  VertexConsumerProvider vertexConsumerProvider, int light,
                  LivingEntity livingEntity, float limbSwing, float limbSwingAmount,
                  float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                IRenderableCurio.RenderHelper.followBodyRotations(livingEntity, model);
                model.setAngles(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                    headPitch);
                VertexConsumer consumer = ItemRenderer
                    .getItemGlintConsumer(vertexConsumerProvider,
                        model.getLayer(KNUCKLES_TEXTURE), false, itemStack.hasGlint());
                model.render(matrixStack, consumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F,
                    1.0F, 1.0F);
              }
            })));
  }
}
