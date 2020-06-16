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

package top.theillusivec4.curios.api.capability;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import top.theillusivec4.curios.api.CurioType;

public interface ICurio {

  /**
   * Called every tick on both client and server while the ItemStack is equipped.
   *
   * @param identifier   The {@link CurioType} identifier of the ItemStack's slot
   * @param index        The index of the slot
   * @param livingEntity The wearer of the ItemStack
   */
  default void curioTick(String identifier, int index, LivingEntity livingEntity) {

  }

  /**
   * Called every tick only on the client while the ItemStack is equipped.
   *
   * @param identifier   The {@link CurioType} identifier of the ItemStack's slot
   * @param index        The index of the slot
   * @param livingEntity The wearer of the ItemStack
   */
  default void curioAnimate(String identifier, int index, LivingEntity livingEntity) {

  }

  /**
   * Called when the ItemStack is equipped into a slot.
   *
   * @param identifier   The {@link CurioType} identifier of the slot being equipped into
   * @param index        The index of the slot
   * @param livingEntity The wearer of the ItemStack
   */
  default void onEquip(String identifier, int index, LivingEntity livingEntity) {

  }

  /**
   * Called when the ItemStack is unequipped from a slot.
   *
   * @param identifier   The {@link CurioType} identifier of the slot being unequipped from
   * @param index        The index of the slot
   * @param livingEntity The wearer of the ItemStack
   */
  default void onUnequip(String identifier, int index, LivingEntity livingEntity) {

  }

  /**
   * Determines if the ItemStack can be equipped into a slot.
   *
   * @param identifier   The {@link CurioType} identifier of the slot being equipped into
   * @param livingEntity The wearer of the ItemStack
   * @return True if the ItemStack can be equipped/put in, false if not
   */
  default boolean canEquip(String identifier, LivingEntity livingEntity) {
    return true;
  }

  /**
   * Determines if the ItemStack can be unequipped from a slot.
   *
   * @param identifier   The {@link CurioType} identifier of the slot being unequipped from
   * @param livingEntity The wearer of the ItemStack
   * @return True if the ItemStack can be unequipped/taken out, false if not
   */
  default boolean canUnequip(String identifier, LivingEntity livingEntity) {
    return true;
  }


  /**
   * Retrieves a list of tooltips when displaying curio tag information. By default, this will be a
   * list of each tag identifier, translated and in gold text, associated with the curio.
   * <br>
   * If overriding, make sure the user has some indication which tags are associated with the
   * curio.
   *
   * @param tagTooltips A list of {@link ITextComponent} with every curio tag
   * @return A list of ITextComponent to display as curio tag information
   */
  default List<ITextComponent> getTagsTooltip(List<ITextComponent> tagTooltips) {
    return tagTooltips;
  }

  /**
   * A map of AttributeModifier associated with the ItemStack and the {@link CurioType} identifier.
   *
   * @param identifier The CurioType identifier for the context
   * @return A map of attribute modifiers to apply
   */
  default Multimap<String, AttributeModifier> getAttributeModifiers(String identifier) {
    return HashMultimap.create();
  }

  /**
   * Plays a sound server-side when a curio is equipped from right-clicking the ItemStack in hand.
   * This can be overridden to play nothing, but it is advised to always play something as an
   * auditory feedback for players.
   *
   * @param livingEntity The wearer of the ItemStack
   */
  default void playRightClickEquipSound(LivingEntity livingEntity) {
    livingEntity.world
        .playSound(null, livingEntity.getPosition(), SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
            SoundCategory.NEUTRAL, 1.0f, 1.0f);
  }

  /**
   * Determines if the ItemStack can be automatically equipped into the first available slot when
   * right-clicked.
   *
   * @return True to enable right-clicking auto-equip, false to disable
   */
  default boolean canRightClickEquip() {
    return false;
  }


  /**
   * Called when rendering break animations and sounds client-side when a worn curio item is
   * broken.
   *
   * @param stack        The ItemStack that was broken
   * @param livingEntity The entity that broke the curio
   */
  default void curioBreak(ItemStack stack, LivingEntity livingEntity) {
    playDefaultBreakSound(stack, livingEntity);
  }

  /**
   * Compares the current ItemStack and the previous ItemStack in the slot to detect any changes and
   * returns true if the change should be synced to all tracking clients. Note that this check
   * occurs every tick so implementations need to code their own timers for other intervals.
   *
   * @param identifier   The identifier of the {@link CurioType} of the slot
   * @param index        The index of the slot
   * @param livingEntity The LivingEntity that is wearing the ItemStack
   * @return True to sync the ItemStack change to all tracking clients, false to do nothing
   */
  default boolean canSync(String identifier, int index, LivingEntity livingEntity) {
    return false;
  }

  /**
   * Gets a tag that is used to sync extra curio data from the server to the client. Only used when
   * {@link ICurio#canSync(String, int, LivingEntity)} returns true.
   *
   * @return Data to be sent to the client
   */
  @Nonnull
  default CompoundNBT writeSyncData() {
    return new CompoundNBT();
  }

  /**
   * Used client-side to read data tags created by {@link ICurio#writeSyncData()} received from the
   * server.
   *
   * @param compound Data received from the server
   */
  default void readSyncData(CompoundNBT compound) {

  }

  /**
   * Determines if the ItemStack should drop on death and persist through respawn. This will persist
   * the ItemStack in the curio slot to the respawned player if applicable.
   *
   * @param livingEntity The entity that died
   * @return {@link DropRule}
   */
  @Nonnull
  default DropRule getDropRule(LivingEntity livingEntity) {
    return DropRule.DEFAULT;
  }

  /**
   * Used by {@link ICurio#getDropRule(LivingEntity)} to determine drop on death behavior.
   * <br>
   * DEFAULT - normal vanilla behavior with drops dictated by the Keep Inventory game rule
   * <br>
   * ALWAYS_DROP - always drop regardless of game rules
   * <br>
   * ALWAYS_KEEP - always keep regardless of game rules
   * <br>
   * DESTROY - destroy the item upon death
   */
  enum DropRule {
    DEFAULT, ALWAYS_DROP, ALWAYS_KEEP, DESTROY
  }

  /**
   * Determines if the ItemStack has rendering.
   *
   * @param identifier   The identifier of the {@link CurioType} of the slot
   * @param index        The index of the slot
   * @param livingEntity The LivingEntity that is wearing the ItemStack
   * @return True if the ItemStack has rendering, false if it does not
   */
  default boolean canRender(String identifier, int index, LivingEntity livingEntity) {
    return false;
  }

  /**
   * Performs rendering of the ItemStack if {@link ICurio#canRender(String, int, LivingEntity)} returns
   * true. Note that vertical sneaking translations are automatically applied before this rendering
   * method is called.
   *
   * @param identifier   The identifier of the {@link CurioType} of the slot
   * @param livingEntity The LivingEntity that is wearing the ItemStack
   */
  default void render(String identifier, int index, MatrixStack matrixStack,
      IRenderTypeBuffer renderTypeBuffer, int light, LivingEntity livingEntity, float limbSwing,
      float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
      float headPitch) {

  }

  static void playDefaultBreakSound(ItemStack stack, LivingEntity livingEntity) {

    if (!stack.isEmpty()) {

      if (!livingEntity.isSilent()) {
        livingEntity.world
            .playSound(livingEntity.getPosX(), livingEntity.getPosY(), livingEntity.getPosZ(),
                SoundEvents.ENTITY_ITEM_BREAK, livingEntity.getSoundCategory(), 0.8F,
                0.8F + livingEntity.world.rand.nextFloat() * 0.4F, false);
      }

      for (int i = 0; i < 5; ++i) {
        Vec3d vec3d = new Vec3d(((double) livingEntity.getRNG().nextFloat() - 0.5D) * 0.1D,
            Math.random() * 0.1D + 0.1D, 0.0D);
        vec3d = vec3d.rotatePitch(-livingEntity.rotationPitch * ((float) Math.PI / 180F));
        vec3d = vec3d.rotateYaw(-livingEntity.rotationYaw * ((float) Math.PI / 180F));
        double d0 = (double) (-livingEntity.getRNG().nextFloat()) * 0.6D - 0.3D;

        Vec3d vec3d1 = new Vec3d(((double) livingEntity.getRNG().nextFloat() - 0.5D) * 0.3D, d0,
            0.6D);
        vec3d1 = vec3d1.rotatePitch(-livingEntity.rotationPitch * ((float) Math.PI / 180F));
        vec3d1 = vec3d1.rotateYaw(-livingEntity.rotationYaw * ((float) Math.PI / 180F));
        vec3d1 = vec3d1.add(livingEntity.getPosX(),
            livingEntity.getPosY() + (double) livingEntity.getEyeHeight(), livingEntity.getPosZ());

        livingEntity.world
            .addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), vec3d1.x, vec3d1.y,
                vec3d1.z, vec3d.x, vec3d.y + 0.05D, vec3d.z);
      }
    }
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
    public static void rotateIfSneaking(final MatrixStack matrixStack,
        final LivingEntity livingEntity) {

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
    public static void followHeadRotations(final LivingEntity livingEntity,
        ModelRenderer... renderers) {

      EntityRenderer<? super LivingEntity> render = Minecraft.getInstance().getRenderManager()
          .getRenderer(livingEntity);

      if (render instanceof LivingRenderer) {
        @SuppressWarnings("unchecked") LivingRenderer<LivingEntity, EntityModel<LivingEntity>> livingRenderer = (LivingRenderer<LivingEntity, EntityModel<LivingEntity>>) render;
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
    public static void followBodyRotations(final LivingEntity livingEntity,
        final BipedModel<LivingEntity>... models) {

      EntityRenderer<? super LivingEntity> render = Minecraft.getInstance().getRenderManager()
          .getRenderer(livingEntity);

      if (render instanceof LivingRenderer) {
        @SuppressWarnings("unchecked") LivingRenderer<LivingEntity, EntityModel<LivingEntity>> livingRenderer = (LivingRenderer<LivingEntity, EntityModel<LivingEntity>>) render;
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
}
