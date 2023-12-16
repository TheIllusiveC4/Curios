/*
 * Copyright (c) 2018-2023 C4
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

package top.theillusivec4.curios.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public interface ICurioRenderer {

  /**
   * Performs rendering of the curio.
   *
   * @param slotContext The slot context of the curio that is being rendered
   */
  <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack,
                                                                 SlotContext slotContext,
                                                                 PoseStack matrixStack,
                                                                 RenderLayerParent<T, M> renderLayerParent,
                                                                 MultiBufferSource renderTypeBuffer,
                                                                 int light, float limbSwing,
                                                                 float limbSwingAmount,
                                                                 float partialTicks,
                                                                 float ageInTicks, float netHeadYaw,
                                                                 float headPitch);

  /**
   * Translates the rendering for the curio if the entity is sneaking.
   *
   * @param livingEntity The wearer of the curio
   */
  static void translateIfSneaking(final PoseStack matrixStack, final LivingEntity livingEntity) {

    if (livingEntity.isCrouching()) {
      matrixStack.translate(0.0F, 0.1875F, 0.0F);
    }
  }

  /**
   * Rotates the rendering for the curio if the entity is sneaking. The rotation angle is based on
   * the body of a player model when sneaking, so this is typically used for items being rendered
   * on the body.
   *
   * @param livingEntity The wearer of the curio
   */
  static void rotateIfSneaking(final PoseStack matrixStack, final LivingEntity livingEntity) {

    if (livingEntity.isCrouching()) {
      EntityRenderer<? super LivingEntity> render =
          Minecraft.getInstance().getEntityRenderDispatcher()
              .getRenderer(livingEntity);

      if (render instanceof LivingEntityRenderer) {
        @SuppressWarnings("unchecked") LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>
            livingRenderer = (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) render;
        EntityModel<LivingEntity> model = livingRenderer.getModel();

        if (model instanceof HumanoidModel) {
          matrixStack.mulPose(Axis.XP.rotation(((HumanoidModel<LivingEntity>) model).body.xRot));
        }
      }
    }
  }

  /**
   * Rotates the rendering for the model renderers based on the entity's head movement. This will
   * align the model renderers with the movements and rotations of the head. This will do nothing
   * if the entity render object does not implement {@link LivingEntityRenderer} or if the model
   * does not have a head (does not implement {@link HumanoidModel}).
   *
   * @param livingEntity The wearer of the curio
   * @param renderers    The list of model renderers to align to the head movement
   */
  static void followHeadRotations(final LivingEntity livingEntity,
                                  final ModelPart... renderers) {

    EntityRenderer<? super LivingEntity> render =
        Minecraft.getInstance().getEntityRenderDispatcher()
            .getRenderer(livingEntity);

    if (render instanceof LivingEntityRenderer) {
      @SuppressWarnings("unchecked") LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>
          livingRenderer = (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) render;
      EntityModel<LivingEntity> model = livingRenderer.getModel();

      if (model instanceof HumanoidModel) {

        for (ModelPart renderer : renderers) {
          renderer.copyFrom(((HumanoidModel<LivingEntity>) model).head);
        }
      }
    }
  }

  /**
   * Rotates the rendering for the models based on the entity's poses and movements. This will do
   * nothing if the entity render object does not implement {@link LivingEntityRenderer} or if the
   * model does not implement {@link HumanoidModel}).
   *
   * @param livingEntity The wearer of the curio
   * @param models       The list of models to align to the body movement
   */
  @SafeVarargs
  static void followBodyRotations(final LivingEntity livingEntity,
                                  final HumanoidModel<LivingEntity>... models) {

    EntityRenderer<? super LivingEntity> render =
        Minecraft.getInstance().getEntityRenderDispatcher()
            .getRenderer(livingEntity);

    if (render instanceof LivingEntityRenderer) {
      @SuppressWarnings("unchecked") LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>
          livingRenderer = (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) render;
      EntityModel<LivingEntity> entityModel = livingRenderer.getModel();

      if (entityModel instanceof HumanoidModel) {

        for (HumanoidModel<LivingEntity> model : models) {
          HumanoidModel<LivingEntity> bipedModel = (HumanoidModel<LivingEntity>) entityModel;
          bipedModel.copyPropertiesTo(model);
        }
      }
    }
  }
}
