package top.theillusivec4.curios.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
      matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F / (float) Math.PI));
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
