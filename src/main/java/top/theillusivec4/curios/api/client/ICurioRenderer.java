/*
 * Copyright (c) 2018-2024 C4
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
 *
 */

package top.theillusivec4.curios.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Axis;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.internal.CuriosClientServices;

/**
 * Logic and data for rendering ItemStack objects found in curio slots on entities.
 */
public interface ICurioRenderer {

  ICurioRenderer DEFAULT = new ICurioRenderer() {
  };

  /**
   * Registers an instance of this interface, given through a supplier, to a specific item.
   *
   * <p>Renderers are loaded from the supplier during
   * {@link net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers} to allow for
   * proper initialization of client and model data in the renderer instance upon construction.
   * The subsequent object that is constructed will be the one assigned to the item, and no more
   * constructions will be called afterward.
   *
   * @param item          The item to associate with the renderer.
   * @param curioRenderer The renderer supplier to call and associate with the item.
   */
  static void register(Item item, Supplier<ICurioRenderer> curioRenderer) {
    CuriosClientServices.EXTENSIONS.registerCurioRenderer(item, curioRenderer);
  }

  /**
   * Gets the renderer instance associated with the item from an ItemStack, or {@link #DEFAULT} if
   * none is found.
   *
   * @param stack The stack to be rendered from the renderer instance.
   * @return The renderer instance, or the default one if none is associated with the item.
   */
  @Nonnull
  static ICurioRenderer get(ItemStack stack) {
    return get(stack.getItem());
  }

  /**
   * Gets the renderer instance associated with an item, or {@link #DEFAULT} if none is found.
   *
   * @param item The item to be rendered from the renderer instance.
   * @return The renderer instance, or the default one if none is associated with the item.
   */
  @Nonnull
  static ICurioRenderer get(Item item) {
    ICurioRenderer renderer = CuriosClientServices.EXTENSIONS.getCurioRenderer(item);
    return renderer == null ? DEFAULT : renderer;
  }

  /**
   * Gets the renderer instance associated with the item from an ItemStack.
   *
   * @param stack The stack to be rendered from the renderer instance.
   * @return The renderer instance, or null if none is associated with the item.
   */
  @Nullable
  static ICurioRenderer getOrNull(ItemStack stack) {
    return getOrNull(stack.getItem());
  }

  /**
   * Gets the renderer instance associated with an item.
   *
   * @param item The item to be rendered from the renderer instance.
   * @return The renderer instance, or null if none is associated with the item.
   */
  @Nullable
  static ICurioRenderer getOrNull(Item item) {
    return CuriosClientServices.EXTENSIONS.getCurioRenderer(item);
  }

  /**
   * Renders an ItemStack in a given SlotContext on an entity.
   *
   * @param stack             The ItemStack being rendered.
   * @param slotContext       The SlotContext for the slot that the item is found in.
   * @param poseStack         The PoseStack containing the current transformations.
   * @param renderTypeBuffer  The buffer for rendering.
   * @param packedLight       The packed light for rendering.
   * @param renderState       The render state of the entity used for this rendering instance.
   * @param renderLayerParent The parent rendering layer and model from the entity.
   * @param context           The rendering context provided by the entity render layer.
   * @param yRotation         The y-rotation of the render state.
   * @param xRotation         The x-rotation of the render state.
   * @param <S>               The class for the entity's render state.
   * @param <M>               The class for the entity's model.
   */
  default <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(
      ItemStack stack,
      SlotContext slotContext,
      PoseStack poseStack,
      @Nonnull MultiBufferSource renderTypeBuffer,
      int packedLight,
      S renderState,
      RenderLayerParent<S, M> renderLayerParent,
      EntityRendererProvider.Context context,
      float yRotation,
      float xRotation) {
    // NO-OP
  }

  /**
   * Renders an ItemStack in a given SlotContext on a player's client if that player is in
   * first-person perspective and one or more of their arms are currently being rendered.
   *
   * <p>This fires in the {@link net.neoforged.neoforge.client.event.RenderArmEvent}. The event
   * can be subscribed to directly, but this method offers a convenient way to do so in the same
   * location as this implementation's other rendering logic.
   *
   * <p>This fires for each arm, and only if that arm is currently being rendered. In addition, the
   * current transformations applied to the arm are already applied in the PoseStack before this
   * method is called.
   *
   * @param stack             The ItemStack being rendered.
   * @param arm               The player's arm being rendered.
   * @param slotContext       The SlotContext for the slot that the item is found in.
   * @param poseStack         The PoseStack containing the current transformations.
   * @param renderTypeBuffer  The buffer for rendering.
   * @param playerRenderState The render state of the player being rendered.
   * @param clientPlayer      The player being rendered.
   * @param packedLight       The packed light for rendering.
   */
  default void renderFirstPersonHand(
      ItemStack stack,
      SlotContext slotContext,
      HumanoidArm arm,
      PoseStack poseStack,
      MultiBufferSource renderTypeBuffer,
      PlayerRenderState playerRenderState,
      AbstractClientPlayer clientPlayer,
      int packedLight) {
    // NO-OP
  }

  /**
   * Renders a model with a texture, optionally with an enchantment glint overlay.
   *
   * @param model            The model to be rendered.
   * @param textureLocation  The location of the texture to be rendered on the model.
   * @param poseStack        The PoseStack containing the current transformations.
   * @param renderTypeBuffer The buffer for rendering.
   * @param packedLight      The packed light for rendering.
   * @param glintRender      The render type of the enchantment glint overlay, or null to disable.
   */
  static void renderModel(
      Model model,
      ResourceLocation textureLocation,
      PoseStack poseStack,
      MultiBufferSource renderTypeBuffer,
      int packedLight,
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
    model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
  }

  /**
   * Applies {@link EntityModel#setupAnim(EntityRenderState)} from a render state on a model. This
   * is useful for applying transformations from a humanoid entity, such as a player, onto a model
   * without making each transformation explicitly.
   *
   * <p>Although the parameter is an {@link LivingEntityRenderState}, this is primarily to avoid
   * casting issues when used by implementations of this interface. The render state must be a
   * child of {@link HumanoidRenderState} or else the method will do nothing.
   *
   * @param model       The model to apply the transformations on.
   * @param renderState The render state to grab the transformation information from.
   */
  @SuppressWarnings("unchecked")
  static void setupHumanoidAnimations(EntityModel<? extends HumanoidRenderState> model,
                                      LivingEntityRenderState renderState) {

    if (renderState instanceof HumanoidRenderState humanoidRenderState) {
      ((EntityModel<HumanoidRenderState>) model).setupAnim(humanoidRenderState);
    }
  }

  /**
   * Applies {@link HumanoidModel#copyPropertiesTo(HumanoidModel)} from the second model to the
   * first model. This is useful for copying the xyz-coordinates, scaling, and rotations from
   * all the parts of a humanoid model to another.
   *
   * <p>Although the parameter is a {@link EntityModel}, this is primarily to avoid casting issues
   * when used by implementations of this interface. The model must be a child of
   * {@link HumanoidModel} or else the method will do nothing.
   *
   * @param model       The original model.
   * @param modelToCopy The model to copy the properties onto the original model.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  static void copyHumanoidProperties(HumanoidModel<?> model, EntityModel<?> modelToCopy) {

    if (modelToCopy instanceof HumanoidModel humanoidModel) {
      humanoidModel.copyPropertiesTo(model);
    }
  }

  /**
   * Renderer that uses a {@link Model} for rendering.
   */
  interface ModelRender<L extends Model> extends ICurioRenderer {

    /**
     * Returns the model used for rendering from an item in a slot.
     *
     * @param stack       The item used for rendering.
     * @param slotContext The context of the slot used for rendering.
     */
    L getModel(ItemStack stack, SlotContext slotContext);

    /**
     * Returns the texture location used for rendering on the model from an item in a slot.
     *
     * @param stack       The item used for rendering.
     * @param slotContext The context of the slot used for rendering.
     */
    ResourceLocation getModelTexture(ItemStack stack, SlotContext slotContext);

    /**
     * Renders the model after all adjustments have been made in
     * {@link ModelRender#prepareModel(ItemStack, SlotContext, PoseStack, MultiBufferSource, int,
     * LivingEntityRenderState, RenderLayerParent, EntityRendererProvider.Context, float, float)}.
     *
     * <p>Enchantment glints will be rendered if {@link ItemStack#hasFoil()} returns true.
     */
    default <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void renderModel(
        ItemStack stack,
        SlotContext slotContext,
        PoseStack poseStack,
        MultiBufferSource renderTypeBuffer,
        int packedLight,
        S renderState,
        RenderLayerParent<S, M> renderLayerParent,
        EntityRendererProvider.Context context,
        float yRotation,
        float xRotation) {
      ICurioRenderer.renderModel(
          this.getModel(stack, slotContext),
          this.getModelTexture(stack, slotContext),
          poseStack,
          renderTypeBuffer,
          packedLight,
          stack.hasFoil() ? RenderType.entityGlint() : null);
    }

    /**
     * Prepares the model for rendering, including adjusting model properties and
     * translations/rotations.
     */
    default <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void prepareModel(
        ItemStack stack,
        SlotContext slotContext,
        PoseStack poseStack,
        MultiBufferSource renderTypeBuffer,
        int packedLight,
        S renderState,
        RenderLayerParent<S, M> renderLayerParent,
        EntityRendererProvider.Context context,
        float yRotation,
        float xRotation) {
      // NO-OP
    }

    @Override
    default <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(
        ItemStack stack,
        SlotContext slotContext,
        PoseStack poseStack,
        @Nonnull MultiBufferSource renderTypeBuffer,
        int packedLight,
        S renderState,
        RenderLayerParent<S, M> renderLayerParent,
        EntityRendererProvider.Context context,
        float yRotation,
        float xRotation) {
      this.prepareModel(
          stack,
          slotContext,
          poseStack,
          renderTypeBuffer,
          packedLight,
          renderState,
          renderLayerParent,
          context,
          yRotation,
          xRotation);
      this.renderModel(
          stack,
          slotContext,
          poseStack,
          renderTypeBuffer,
          packedLight,
          renderState,
          renderLayerParent,
          context,
          yRotation,
          xRotation);
    }
  }

  /**
   * Renderer that uses a {@link HumanoidModel} for rendering.
   *
   * <p>The default methods will call {@link #copyHumanoidProperties(HumanoidModel, EntityModel)}
   * and {@link #setupHumanoidAnimations(EntityModel, LivingEntityRenderState)} on the model
   * before rendering.
   *
   * <p>This also implements
   * {@link #renderFirstPersonHand(ItemStack, SlotContext, HumanoidArm, PoseStack,
   * MultiBufferSource, PlayerRenderState, AbstractClientPlayer, int)} with the same rendering that
   * is performed in {@link #render(ItemStack, SlotContext, PoseStack, MultiBufferSource, int,
   * LivingEntityRenderState, RenderLayerParent, EntityRendererProvider.Context, float, float)}.
   */
  interface HumanoidRender extends ModelRender<HumanoidModel<? extends HumanoidRenderState>> {

    @Override
    default <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void prepareModel(
        ItemStack stack,
        SlotContext slotContext,
        PoseStack poseStack,
        MultiBufferSource renderTypeBuffer,
        int packedLight,
        S renderState,
        RenderLayerParent<S, M> renderLayerParent,
        EntityRendererProvider.Context context,
        float yRotation,
        float xRotation) {
      HumanoidModel<? extends HumanoidRenderState> model = this.getModel(stack, slotContext);
      M parentModel = renderLayerParent.getModel();
      ICurioRenderer.copyHumanoidProperties(model, parentModel);
      ICurioRenderer.setupHumanoidAnimations(model, renderState);
    }

    @Override
    default <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void renderModel(
        ItemStack stack,
        SlotContext slotContext,
        PoseStack poseStack,
        MultiBufferSource renderTypeBuffer,
        int packedLight,
        S renderState,
        RenderLayerParent<S, M> renderLayerParent,
        EntityRendererProvider.Context context,
        float yRotation,
        float xRotation) {
      ICurioRenderer.renderModel(
          this.getModel(stack, slotContext),
          this.getModelTexture(stack, slotContext),
          poseStack,
          renderTypeBuffer,
          packedLight,
          stack.hasFoil() ? RenderType.armorEntityGlint() : null);
    }

    @Override
    default void renderFirstPersonHand(ItemStack stack,
                                       SlotContext slotContext,
                                       HumanoidArm arm,
                                       PoseStack poseStack,
                                       MultiBufferSource renderTypeBuffer,
                                       PlayerRenderState playerRenderState,
                                       AbstractClientPlayer clientPlayer,
                                       int packedLight) {
      HumanoidModel<? extends HumanoidRenderState> model = this.getModel(stack, slotContext);
      ICurioRenderer.setupHumanoidAnimations(model, playerRenderState);
      model.resetPose();
      ICurioRenderer.renderModel(
          this.getModel(stack, slotContext),
          this.getModelTexture(stack, slotContext),
          poseStack,
          renderTypeBuffer,
          packedLight,
          stack.hasFoil() ? RenderType.armorEntityGlint() : null);
    }
  }

  /**
   * Translates the rendering for the curio if the entity is sneaking.
   *
   * @param livingEntity The wearer of the curio
   * @deprecated Use {@link #setupHumanoidAnimations(EntityModel, LivingEntityRenderState)} instead
   *     for a more robust and complete method to apply all transformations from an entity.
   */
  @Deprecated(forRemoval = true)
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
   * @deprecated Use {@link #setupHumanoidAnimations(EntityModel, LivingEntityRenderState)} instead
   *     for a more robust and complete method to apply all transformations from an entity.
   */
  @Deprecated(forRemoval = true)
  static void rotateIfSneaking(final PoseStack matrixStack, final LivingEntity livingEntity) {

    if (livingEntity.isCrouching()) {
      EntityModel<LivingEntityRenderState> entityModel = getModelFromEntity(livingEntity);

      if (entityModel instanceof HumanoidModel<?> humanoidModel) {
        matrixStack.mulPose(Axis.XP.rotation(humanoidModel.body.xRot));
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
   * @deprecated Use {@link #setupHumanoidAnimations(EntityModel, LivingEntityRenderState)} instead
   *     for a more robust and complete method to apply all transformations from an entity.
   */
  @Deprecated(forRemoval = true)
  static void followHeadRotations(final LivingEntity livingEntity,
                                  final ModelPart... renderers) {
    EntityModel<LivingEntityRenderState> entityModel = getModelFromEntity(livingEntity);

    if (entityModel instanceof HumanoidModel<?> humanoidModel) {

      for (ModelPart renderer : renderers) {
        renderer.copyFrom(humanoidModel.head);
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
   * @deprecated Use {@link #setupHumanoidAnimations(EntityModel, LivingEntityRenderState)} instead
   *     for a more robust and complete method to apply all transformations from an entity.
   */
  @Deprecated(forRemoval = true)
  @SafeVarargs
  @SuppressWarnings("unchecked")
  static void followBodyRotations(final LivingEntity livingEntity,
                                  final HumanoidModel<HumanoidRenderState>... models) {
    EntityModel<LivingEntityRenderState> entityModel = getModelFromEntity(livingEntity);

    if (entityModel instanceof HumanoidModel<?> humanoidModel) {

      for (HumanoidModel<HumanoidRenderState> model : models) {
        HumanoidModel<HumanoidRenderState> bipedModel =
            (HumanoidModel<HumanoidRenderState>) humanoidModel;
        bipedModel.copyPropertiesTo(model);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static EntityModel<LivingEntityRenderState> getModelFromEntity(
      LivingEntity livingEntity) {
    EntityRenderer<? super LivingEntity, ?> render =
        Minecraft.getInstance().getEntityRenderDispatcher()
            .getRenderer(livingEntity);

    if (!(render instanceof LivingEntityRenderer)) {
      return null;
    }
    LivingEntityRenderer<
        LivingEntity,
        LivingEntityRenderState,
        EntityModel<LivingEntityRenderState>> livingRenderer =
        (LivingEntityRenderer<
            LivingEntity,
            LivingEntityRenderState,
            EntityModel<LivingEntityRenderState>>) render;
    return livingRenderer.getModel();
  }
}
