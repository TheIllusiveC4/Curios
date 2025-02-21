package top.theillusivec4.curios.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public interface ICurioRenderer {

  /**
   * Performs rendering of the curio.
   *
   * @param slotContext The slot context of the curio that is being rendered
   */
  <T extends LivingEntity, M extends EntityModel<T>> void render(
      ItemStack stack,
      SlotContext slotContext,
      PoseStack matrixStack,
      RenderLayerParent<T, M> renderLayerParent,
      MultiBufferSource renderTypeBuffer,
      int light,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
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
   * the body of a player model when sneaking, so this is typically used for items being rendered on
   * the body.
   *
   * @param livingEntity The wearer of the curio
   */
  static void rotateIfSneaking(final PoseStack matrixStack, final LivingEntity livingEntity) {

    if (livingEntity.isCrouching()) {
      EntityRenderer<? super LivingEntity> render =
          Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity);

      if (render instanceof LivingEntityRenderer) {
        @SuppressWarnings("unchecked")
        LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> livingRenderer =
            (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) render;
        EntityModel<LivingEntity> model = livingRenderer.getModel();

        if (model instanceof net.minecraft.client.model.HumanoidModel) {
          matrixStack.mulPose(
              Axis.XP.rotation(
                  ((net.minecraft.client.model.HumanoidModel<LivingEntity>) model).body.xRot));
        }
      }
    }
  }

  /**
   * Rotates the rendering for the model renderers based on the entity's head movement. This will
   * align the model renderers with the movements and rotations of the head. This will do nothing if
   * the entity render object does not implement {@link LivingEntityRenderer} or if the model does
   * not have a head (does not implement {@link net.minecraft.client.model.HumanoidModel}).
   *
   * @param livingEntity The wearer of the curio
   * @param renderers The list of model renderers to align to the head movement
   */
  static void followHeadRotations(final LivingEntity livingEntity, final ModelPart... renderers) {

    EntityRenderer<? super LivingEntity> render =
        Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity);

    if (render instanceof LivingEntityRenderer) {
      @SuppressWarnings("unchecked")
      LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> livingRenderer =
          (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) render;
      EntityModel<LivingEntity> model = livingRenderer.getModel();

      if (model instanceof net.minecraft.client.model.HumanoidModel) {

        for (ModelPart renderer : renderers) {
          renderer.copyFrom(((net.minecraft.client.model.HumanoidModel<LivingEntity>) model).head);
        }
      }
    }
  }

  /**
   * Rotates the rendering for the models based on the entity's poses and movements. This will do
   * nothing if the entity render object does not implement {@link LivingEntityRenderer} or if the
   * model does not implement {@link net.minecraft.client.model.HumanoidModel}).
   *
   * @param livingEntity The wearer of the curio
   * @param models The list of models to align to the body movement
   */
  @SafeVarargs
  static void followBodyRotations(
      final LivingEntity livingEntity,
      final net.minecraft.client.model.HumanoidModel<LivingEntity>... models) {

    EntityRenderer<? super LivingEntity> render =
        Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity);

    if (render instanceof LivingEntityRenderer) {
      @SuppressWarnings("unchecked")
      LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> livingRenderer =
          (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) render;
      EntityModel<LivingEntity> entityModel = livingRenderer.getModel();

      if (entityModel instanceof net.minecraft.client.model.HumanoidModel) {

        for (net.minecraft.client.model.HumanoidModel<LivingEntity> model : models) {
          net.minecraft.client.model.HumanoidModel<LivingEntity> bipedModel =
              (net.minecraft.client.model.HumanoidModel<LivingEntity>) entityModel;
          bipedModel.copyPropertiesTo(model);
        }
      }
    }
  }

  /**
   * Renders a model with a texture, optionally with an enchantment glint overlay.
   *
   * @param model The model to be rendered
   * @param textureLocation The location of the texture to be rendered on the model
   * @param glintRender The render type of the enchantment glint overlay, or null to disable
   */
  static void renderModel(
      Model model,
      ResourceLocation textureLocation,
      PoseStack poseStack,
      MultiBufferSource renderTypeBuffer,
      int light,
      @Nullable RenderType glintRender) {
    RenderType renderType = model.renderType(textureLocation);
    VertexConsumer vertexConsumer;

    if (glintRender != null) {
      vertexConsumer =
          VertexMultiConsumer.create(
              renderTypeBuffer.getBuffer(glintRender), renderTypeBuffer.getBuffer(renderType));
    } else {
      vertexConsumer = renderTypeBuffer.getBuffer(renderType);
    }
    model.renderToBuffer(
        poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
  }

  /**
   * A helper interface to streamline the rendering of a curio item that uses a {@link Model}
   * object.
   */
  interface ModelRender<L extends Model> extends ICurioRenderer {

    /**
     * Returns the model used for rendering based on an item and slot combination.
     *
     * @param stack The item used for rendering.
     * @param slotContext The context of the slot used for rendering.
     */
    L getModel(ItemStack stack, SlotContext slotContext);

    /**
     * Returns the texture location used for rendering on the model, based on an item and slot
     * combination.
     *
     * @param stack The item used for rendering.
     * @param slotContext The context of the slot used for rendering.
     */
    ResourceLocation getModelTexture(ItemStack stack, SlotContext slotContext);

    /**
     * Renders the model after all adjustments have been made in {@link
     * ModelRender#prepareModel(ItemStack, SlotContext, PoseStack, RenderLayerParent, float, float,
     * float, float, float, float)}.
     */
    default void renderModel(
        ItemStack stack,
        SlotContext slotContext,
        PoseStack poseStack,
        RenderLayerParent<LivingEntity, EntityModel<LivingEntity>> renderLayerParent,
        MultiBufferSource renderTypeBuffer,
        int light) {
      ICurioRenderer.renderModel(
          this.getModel(stack, slotContext),
          this.getModelTexture(stack, slotContext),
          poseStack,
          renderTypeBuffer,
          light,
          stack.hasFoil() ? RenderType.entityGlint() : null);
    }

    /**
     * Prepares the model for rendering, including adjusting model properties and
     * translations/rotations.
     */
    default void prepareModel(
        ItemStack stack,
        SlotContext slotContext,
        PoseStack poseStack,
        RenderLayerParent<LivingEntity, EntityModel<LivingEntity>> renderLayerParent,
        float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch) {}

    @SuppressWarnings("unchecked")
    @Override
    default <T extends LivingEntity, M extends EntityModel<T>> void render(
        ItemStack stack,
        SlotContext slotContext,
        PoseStack poseStack,
        RenderLayerParent<T, M> renderLayerParent,
        MultiBufferSource renderTypeBuffer,
        int light,
        float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch) {
      this.prepareModel(
          stack,
          slotContext,
          poseStack,
          (RenderLayerParent<LivingEntity, EntityModel<LivingEntity>>) renderLayerParent,
          limbSwing,
          limbSwingAmount,
          partialTicks,
          ageInTicks,
          netHeadYaw,
          headPitch);
      this.renderModel(
          stack,
          slotContext,
          poseStack,
          (RenderLayerParent<LivingEntity, EntityModel<LivingEntity>>) renderLayerParent,
          renderTypeBuffer,
          light);
    }
  }

  /**
   * A helper interface to streamline the rendering of a curio item that uses a {@link
   * HumanoidModel} object.
   *
   * <p>By default, the rendered model will copy the properties of the entity model wearing the
   * curio item (i.e. model parts will follow entity movements as defined by its HumanoidModel
   * definition).
   */
  interface HumanoidRender extends ModelRender<HumanoidModel<LivingEntity>> {

    @Override
    default void prepareModel(
        ItemStack stack,
        SlotContext slotContext,
        PoseStack poseStack,
        RenderLayerParent<LivingEntity, EntityModel<LivingEntity>> renderLayerParent,
        float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch) {
      HumanoidModel<LivingEntity> model = this.getModel(stack, slotContext);
      LivingEntity livingEntity = slotContext.entity();
      EntityModel<LivingEntity> parentModel = renderLayerParent.getModel();

      if (parentModel instanceof HumanoidModel<LivingEntity> humanoidModel) {
        humanoidModel.copyPropertiesTo(model);
      } else {
        parentModel.copyPropertiesTo(model);
      }
      model.prepareMobModel(livingEntity, limbSwing, limbSwingAmount, partialTicks);
      model.setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    default void renderModel(
        ItemStack stack,
        SlotContext slotContext,
        PoseStack poseStack,
        RenderLayerParent<LivingEntity, EntityModel<LivingEntity>> renderLayerParent,
        MultiBufferSource renderTypeBuffer,
        int light) {
      ICurioRenderer.renderModel(
          this.getModel(stack, slotContext),
          this.getModelTexture(stack, slotContext),
          poseStack,
          renderTypeBuffer,
          light,
          stack.hasFoil() ? RenderType.armorEntityGlint() : null);
    }
  }
}
