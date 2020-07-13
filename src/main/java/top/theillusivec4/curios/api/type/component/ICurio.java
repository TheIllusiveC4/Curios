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
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
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
import top.theillusivec4.curios.api.CuriosComponent;
import top.theillusivec4.curios.api.type.ISlotType;

public interface ICurio extends CopyableComponent<ICurio> {

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

  @Override
  default void fromTag(CompoundTag var1) {

  }

  @Override
  default CompoundTag toTag(CompoundTag var1) {
    return new CompoundTag();
  }

  @Override
  default boolean isComponentEqual(Component other) {
    return true;
  }

  @Override
  default ComponentType<ICurio> getComponentType() {
    return CuriosComponent.ITEM;
  }
}
