package top.theillusivec4.curios.api.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import top.theillusivec4.curios.api.SlotContext;

public interface ICurioRenderer {

  /**
   * Performs rendering of the curio.
   * Note that vertical sneaking translations are automatically applied before this rendering method
   * is called.
   *
   * @param slotContext The slot context of the curio that is being rendered
   */
  <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack,
                                                                 SlotContext slotContext,
                                                                 IEntityRenderer<T, M> entityRenderer,
                                                                 MatrixStack matrixStack,
                                                                 IRenderTypeBuffer renderTypeBuffer,
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
  static void translateIfSneaking(final MatrixStack matrixStack, final LivingEntity livingEntity) {

    if (livingEntity.isCrouching()) {
      matrixStack.translate(0.0f, 0.2f, 0.0f);
    }
  }

  /**
   * Rotates the rendering for the curio if the entity is sneaking. The rotation angle is based on
   * the body of a player model when sneaking, so this is typically used for items being rendered
   * on the body.
   *
   * @param livingEntity The wearer of the curio
   */
  static void rotateIfSneaking(final MatrixStack matrixStack, final LivingEntity livingEntity) {

    if (livingEntity.isCrouching()) {
      matrixStack.rotate(Vector3f.XP.rotationDegrees(90.0F / (float) Math.PI));
    }
  }

  /**
   * Rotates the rendering for the model renderers based on the entity's head movement. This will
   * align the model renderers with the movements and rotations of the head. This will do nothing
   * if the entity render object does not implement {@link LivingRenderer} or if the model does
   * not have a head (does not implement {@link BipedModel}).
   *
   * @param livingEntity The wearer of the curio
   * @param renderers    The list of model renderers to align to the head movement
   */
  static void followHeadRotations(final LivingEntity livingEntity,
                                  final ModelRenderer... renderers) {

    EntityRenderer<? super LivingEntity> render = Minecraft.getInstance().getRenderManager()
        .getRenderer(livingEntity);

    if (render instanceof LivingRenderer) {
      @SuppressWarnings("unchecked") LivingRenderer<LivingEntity, EntityModel<LivingEntity>>
          livingRenderer = (LivingRenderer<LivingEntity, EntityModel<LivingEntity>>) render;
      EntityModel<LivingEntity> model = livingRenderer.getEntityModel();

      if (model instanceof BipedModel) {

        for (ModelRenderer renderer : renderers) {
          renderer.copyModelAngles(((BipedModel<LivingEntity>) model).bipedHead);
        }
      }
    }
  }

  /**
   * Rotates the rendering for the models based on the entity's poses and movements. This will do
   * nothing if the entity render object does not implement {@link LivingRenderer} or if the model
   * does not implement {@link BipedModel}).
   *
   * @param livingEntity The wearer of the curio
   * @param models       The list of models to align to the body movement
   */
  @SafeVarargs
  static void followBodyRotations(final LivingEntity livingEntity,
                                  final BipedModel<LivingEntity>... models) {

    EntityRenderer<? super LivingEntity> render = Minecraft.getInstance().getRenderManager()
        .getRenderer(livingEntity);

    if (render instanceof LivingRenderer) {
      @SuppressWarnings("unchecked") LivingRenderer<LivingEntity, EntityModel<LivingEntity>>
          livingRenderer = (LivingRenderer<LivingEntity, EntityModel<LivingEntity>>) render;
      EntityModel<LivingEntity> entityModel = livingRenderer.getEntityModel();

      if (entityModel instanceof BipedModel) {

        for (BipedModel<LivingEntity> model : models) {
          BipedModel<LivingEntity> bipedModel = (BipedModel<LivingEntity>) entityModel;
          bipedModel.setModelAttributes(model);
        }
      }
    }
  }
}
