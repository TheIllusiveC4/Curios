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

package top.theillusivec4.curios.api.type.component;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
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
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import top.theillusivec4.curios.api.type.ISlotType;

public interface ICurio extends Component {

  /*
   * Copy of vanilla implementation for breaking items client-side
   */
  static void playBreakAnimation(ItemStack stack, LivingEntity livingEntity) {

    if (!stack.isEmpty()) {

      if (!livingEntity.isSilent()) {
        livingEntity.world.playSound(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
            SoundEvents.ENTITY_ITEM_BREAK, livingEntity.getSoundCategory(), 0.8F,
            0.8F + livingEntity.world.random.nextFloat() * 0.4F, false);
      }

      for (int i = 0; i < 5; ++i) {
        Vec3d vec3d = new Vec3d(((double) livingEntity.getRandom().nextFloat() - 0.5D) * 0.1D,
            Math.random() * 0.1D + 0.1D, 0.0D);
        vec3d = vec3d.rotateX(-livingEntity.pitch * ((float) Math.PI / 180F));
        vec3d = vec3d.rotateY(-livingEntity.yaw * ((float) Math.PI / 180F));
        double d0 = (double) (-livingEntity.getRandom().nextFloat()) * 0.6D - 0.3D;

        Vec3d vec3d1 = new Vec3d(((double) livingEntity.getRandom().nextFloat() - 0.5D) * 0.3D, d0,
            0.6D);
        vec3d1 = vec3d1.rotateX(-livingEntity.pitch * ((float) Math.PI / 180F));
        vec3d1 = vec3d1.rotateY(-livingEntity.yaw * ((float) Math.PI / 180F));
        vec3d1 = vec3d1.add(livingEntity.getX(), livingEntity.getY() + livingEntity.getEyeY(),
            livingEntity.getZ());

        livingEntity.world
            .addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), vec3d1.x, vec3d1.y,
                vec3d1.z, vec3d.x, vec3d.y + 0.05D, vec3d.z);
      }
    }
  }

  /**
   * Called every tick on both client and server while the ItemStack is equipped.
   *
   * @param identifier   The {@link ISlotType} identifier of the ItemStack's slot
   * @param index        The index of the slot
   * @param livingEntity The wearer of the ItemStack
   */
  default void curioTick(String identifier, int index, LivingEntity livingEntity) {

  }

  /**
   * Called every tick only on the client while the ItemStack is equipped.
   *
   * @param identifier   The {@link ISlotType} identifier of the ItemStack's slot
   * @param index        The index of the slot
   * @param livingEntity The wearer of the ItemStack
   */
  default void curioAnimate(String identifier, int index, LivingEntity livingEntity) {

  }

  /**
   * Called when the ItemStack is equipped into a slot.
   *
   * @param identifier   The {@link ISlotType} identifier of the slot being equipped into
   * @param index        The index of the slot
   * @param livingEntity The wearer of the ItemStack
   */
  default void onEquip(String identifier, int index, LivingEntity livingEntity) {

  }

  /**
   * Called when the ItemStack is unequipped from a slot.
   *
   * @param identifier   The {@link ISlotType} identifier of the slot being unequipped from
   * @param index        The index of the slot
   * @param livingEntity The wearer of the ItemStack
   */
  default void onUnequip(String identifier, int index, LivingEntity livingEntity) {

  }

  /**
   * Determines if the ItemStack can be equipped into a slot.
   *
   * @param identifier   The {@link ISlotType} identifier of the slot being equipped into
   * @param livingEntity The wearer of the ItemStack
   * @return True if the ItemStack can be equipped/put in, false if not
   */
  default boolean canEquip(String identifier, LivingEntity livingEntity) {
    return true;
  }

  /**
   * Determines if the ItemStack can be unequipped from a slot.
   *
   * @param identifier   The {@link ISlotType} identifier of the slot being unequipped from
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
   * @param tagTooltips A list of {@link Text} with every curio tag
   * @return A list of ITextComponent to display as curio tag information
   */
  default List<Text> getTagsTooltip(List<Text> tagTooltips) {
    return tagTooltips;
  }

  /**
   * A map of AttributeModifier associated with the ItemStack and the {@link ISlotType} identifier.
   *
   * @param identifier The CurioType identifier for the context
   * @return A map of attribute modifiers to apply
   */
  default Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(
      String identifier) {
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
        .playSound(null, livingEntity.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
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
    playBreakAnimation(stack, livingEntity);
  }

  /**
   * Compares the current ItemStack and the previous ItemStack in the slot to detect any changes and
   * returns true if the change should be synced to all tracking clients. Note that this check
   * occurs every tick so implementations need to code their own timers for other intervals.
   *
   * @param identifier   The identifier of the {@link ISlotType} of the slot
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
  default CompoundTag writeSyncData() {
    return new CompoundTag();
  }

  /**
   * Used client-side to read data tags created by {@link ICurio#writeSyncData()} received from the
   * server.
   *
   * @param compound Data received from the server
   */
  default void readSyncData(CompoundTag compound) {

  }

  /**
   * Determines if the ItemStack should drop on death and persist through respawn. This will persist
   * the ItemStack in the curio slot to the respawned player if applicable.
   *
   * @param livingEntity The entity that died
   * @return {@link DropRule}
   */
  default DropRule getDropRule(LivingEntity livingEntity) {
    return DropRule.DEFAULT;
  }

  /**
   * Determines if the ItemStack has rendering.
   *
   * @param identifier   The identifier of the {@link ISlotType} of the slot
   * @param index        The index of the slot
   * @param livingEntity The LivingEntity that is wearing the ItemStack
   * @return True if the ItemStack has rendering, false if it does not
   */
  default boolean canRender(String identifier, int index, LivingEntity livingEntity) {
    return false;
  }

  /**
   * Performs rendering of the ItemStack if {@link ICurio#canRender(String, int, LivingEntity)}
   * returns true. Note that vertical sneaking translations are automatically applied before this
   * rendering method is called.
   *
   * @param identifier   The identifier of the {@link ISlotType} of the slot
   * @param livingEntity The LivingEntity that is wearing the ItemStack
   */
  default void render(String identifier, int index, MatrixStack matrixStack,
      VertexConsumerProvider vertexConsumerProvider, int light, LivingEntity livingEntity,
      float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
      float netHeadYaw, float headPitch) {

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
