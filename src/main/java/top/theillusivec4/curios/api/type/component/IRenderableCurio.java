package top.theillusivec4.curios.api.type.component;

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import top.theillusivec4.curios.api.type.ISlotType;

public interface IRenderableCurio extends Component {

  /**
   * Performs rendering of the ItemStack. Note that vertical sneaking translations are automatically
   * applied before this rendering method is called.
   *
   * @param identifier   The identifier of the {@link ISlotType} of the slot
   * @param livingEntity The LivingEntity that is wearing the ItemStack
   */
  default void render(String identifier, int index, MatrixStack matrixStack,
      VertexConsumerProvider vertexConsumerProvider, int light, LivingEntity livingEntity,
      float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
      float netHeadYaw, float headPitch) {

  }

  @Override
  default void fromTag(CompoundTag var1) {

  }

  @Override
  default CompoundTag toTag(CompoundTag var1) {
    return new CompoundTag();
  }

  /**
   * Some helper methods for rendering curios.
   */
  final class RenderHelper {

    /**
     * Translates the rendering for the curio if the entity is sneaking.
     *
     * @param livingEntity The wearer of the curio
     */
    public static void translateIfSneaking(final MatrixStack matrixStack,
        final LivingEntity livingEntity) {

      if (livingEntity.isSneaking()) {
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
    public static void rotateIfSneaking(final MatrixStack matrixStack,
        final LivingEntity livingEntity) {

      if (livingEntity.isSneaking()) {
        matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90.0F / (float) Math.PI));
      }
    }

    /**
     * Rotates the rendering for the model renderers based on the entity's head movement. This will
     * align the model renderers with the movements and rotations of the head. This will do nothing
     * if the entity render object does not implement {@link LivingEntityRenderer} or if the model
     * does not have a head (does not implement {@link BipedEntityModel}).
     *
     * @param livingEntity The wearer of the curio
     * @param parts        The list of model renderers to align to the head movement
     */
    public static void followHeadRotations(final LivingEntity livingEntity, ModelPart... parts) {

      EntityRenderer<? super LivingEntity> render = MinecraftClient.getInstance()
          .getEntityRenderManager().getRenderer(livingEntity);

      if (render instanceof LivingEntityRenderer) {
        @SuppressWarnings("unchecked") LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> livingRenderer = (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) render;
        EntityModel<LivingEntity> model = livingRenderer.getModel();

        if (model instanceof BipedEntityModel) {

          for (ModelPart part : parts) {
            part.copyPositionAndRotation(((BipedEntityModel<LivingEntity>) model).head);
          }
        }
      }
    }

    /**
     * Rotates the rendering for the models based on the entity's poses and movements. This will do
     * nothing if the entity render object does not implement {@link LivingEntityRenderer} or if the
     * model does not implement {@link BipedEntityModel}).
     *
     * @param livingEntity The wearer of the curio
     * @param models       The list of models to align to the body movement
     */
    @SafeVarargs
    public static void followBodyRotations(final LivingEntity livingEntity,
        final BipedEntityModel<LivingEntity>... models) {

      EntityRenderer<? super LivingEntity> render = MinecraftClient.getInstance()
          .getEntityRenderManager().getRenderer(livingEntity);

      if (render instanceof LivingEntityRenderer) {
        @SuppressWarnings("unchecked") LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> livingRenderer = (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) render;
        EntityModel<LivingEntity> entityModel = livingRenderer.getModel();

        if (entityModel instanceof BipedEntityModel) {

          for (BipedEntityModel<LivingEntity> model : models) {
            BipedEntityModel<LivingEntity> bipedModel = (BipedEntityModel<LivingEntity>) entityModel;
            bipedModel.setAttributes(model);
          }
        }
      }
    }
  }
}
