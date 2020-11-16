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

package top.theillusivec4.curios.api.type.capability;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurio.DropRule;
import net.minecraft.item.Item;

/**
 * Designed to be directly implemented on {@link Item} objects.<br/><br/>
 * Curios will automatically create and attach {@link ICurio} capability to any ItemStacks that contain items
 * implementing this interface, redirecting all calls made on such capability to respective methods here.
 * @author Extegral
 */

public interface IItemCurio {

  /**
   * Default instance of {@link ICurio}, where all calls are redirected by default methods
   * of this interface to avoid needlessly copying over code from there.
   */

  public static final ICurio defaultInstance = new ICurio(){};


  /**
   * Called during automatic capability attachment to any ItemStack containing this {@link IItemCurio} instance.
   * @param stack ItemStack in question
   * @return true to allow attach {@link ICurio} capability to this ItemStack; false to prevent attachment.
   */

  default boolean attachCapability(ItemStack stack) {
	return true;
  }

  /**
   * Called every tick on both client and server while the ItemStack is equipped.
   *
   * @param identifier   The {@link ISlotType} identifier of the ItemStack's slot
   * @param index        The index of the slot
   * @param livingEntity The wearer of the ItemStack
   * @param stack        The ItemStack in question
   */
  default void curioTick(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {
	defaultInstance.curioTick(identifier, index, livingEntity);
  }

  /**
   * Called every tick only on the client while the ItemStack is equipped.
   *
   * @param identifier   The {@link ISlotType} identifier of the ItemStack's slot
   * @param index        The index of the slot
   * @param livingEntity The wearer of the ItemStack
   * @param stack        The ItemStack in question
   */
  default void curioAnimate(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {
	defaultInstance.curioAnimate(identifier, index, livingEntity);
  }

  /**
   * Called when the ItemStack is equipped into a slot.
   *
   * @param identifier   The {@link ISlotType} identifier of the slot being
   *                     equipped into
   * @param index        The index of the slot
   * @param livingEntity The wearer of the ItemStack
   * @param stack        The ItemStack in question
   */
  default void onEquip(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {
	defaultInstance.onEquip(identifier, index, livingEntity);;
  }

  /**
   * Called when the ItemStack is unequipped from a slot.
   *
   * @param identifier   The {@link ISlotType} identifier of the slot being
   *                     unequipped from
   * @param index        The index of the slot
   * @param livingEntity The wearer of the ItemStack
   * @param stack        The ItemStack in question
   */
  default void onUnequip(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {
	defaultInstance.onUnequip(identifier, index, livingEntity);
  }

  /**
   * Determines if the ItemStack can be equipped into a slot.
   *
   * @param identifier   The {@link ISlotType} identifier of the slot being
   *                     equipped into
   * @param livingEntity The wearer of the ItemStack
   * @param stack        The ItemStack in question
   * @return True if the ItemStack can be equipped/put in, false if not
   */
  default boolean canEquip(String identifier, LivingEntity livingEntity, ItemStack stack) {
	return defaultInstance.canEquip(identifier, livingEntity);
  }

  /**
   * Determines if the ItemStack can be unequipped from a slot.
   *
   * @param identifier   The {@link ISlotType} identifier of the slot being
   *                     unequipped from
   * @param livingEntity The wearer of the ItemStack
   * @param stack        The ItemStack in question
   * @return True if the ItemStack can be unequipped/taken out, false if not
   */
  default boolean canUnequip(String identifier, LivingEntity livingEntity, ItemStack stack) {
	return defaultInstance.canUnequip(identifier, livingEntity);
  }

  /**
   * Retrieves a list of tooltips when displaying curio tag information. By
   * default, this will be a list of each tag identifier, translated and in gold
   * text, associated with the curio. <br>
   * If overriding, make sure the user has some indication which tags are
   * associated with the curio.
   *
   * @param tagTooltips A list of {@link ITextComponent} with every curio tag
   * @return A list of ITextComponent to display as curio tag information
   */
  default List<ITextComponent> getTagsTooltip(List<ITextComponent> tagTooltips) {
	return defaultInstance.getTagsTooltip(tagTooltips);
  }

  /**
   * A map of AttributeModifier associated with the ItemStack and the
   * {@link ISlotType} identifier.
   *
   * @param identifier The CurioType identifier for the context
   * @param stack      The ItemStack in question
   * @return A map of attribute modifiers to apply
   */
  default Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier, ItemStack stack) {
	return defaultInstance.getAttributeModifiers(identifier);
  }

  /**
   * Plays a sound server-side when a curio is equipped from right-clicking the
   * ItemStack in hand. This can be overridden to play nothing, but it is advised
   * to always play something as an auditory feedback for players.
   *
   * @param identifier Identifier of slot into which stack is equipped
   * @param stack      The ItemStack in question
   */
  default void playRightClickEquipSound(LivingEntity livingEntity, ItemStack stack) {
	defaultInstance.playRightClickEquipSound(livingEntity);
  }

  /**
   * Determines if the ItemStack can be automatically equipped into the first
   * available slot when right-clicked.
   *
   * @param stack The ItemStack in question
   * @return True to enable right-clicking auto-equip, false to disable
   */
  default boolean canRightClickEquip(ItemStack stack) {
	return defaultInstance.canRightClickEquip();
  }

  /**
   * Called when rendering break animations and sounds client-side when a worn
   * curio item is broken.
   *
   * @param stack        The ItemStack that was broken
   * @param livingEntity The entity that broke the curio
   */
  default void curioBreak(ItemStack stack, LivingEntity livingEntity) {
	defaultInstance.curioBreak(stack, livingEntity);
  }

  /**
   * Compares the current ItemStack and the previous ItemStack in the slot to
   * detect any changes and returns true if the change should be synced to all
   * tracking clients. Note that this check occurs every tick so implementations
   * need to code their own timers for other intervals.
   *
   * @param identifier   The identifier of the {@link ISlotType} of the slot
   * @param index        The index of the slot
   * @param livingEntity The LivingEntity that is wearing the ItemStack
   * @return True to sync the ItemStack change to all tracking clients, false to
   *         do nothing
   */
  default boolean canSync(String identifier, int index, LivingEntity livingEntity) {
	return defaultInstance.canSync(identifier, index, livingEntity);
  }

  /**
   * Gets a tag that is used to sync extra curio data from the server to the
   * client. Only used when {@link IItemCurio#canSync(String, int, LivingEntity)}
   * returns true.
   *
   * @return Data to be sent to the client
   */
  @Nonnull
  default CompoundNBT writeSyncData() {
	return defaultInstance.writeSyncData();
  }

  /**
   * Used client-side to read data tags created by
   * {@link IItemCurio#writeSyncData()} received from the server.
   *
   * @param compound Data received from the server
   */
  default void readSyncData(CompoundNBT compound) {
	defaultInstance.readSyncData(compound);
  }

  /**
   * Determines if the ItemStack should drop on death and persist through respawn.
   * This will persist the ItemStack in the curio slot to the respawned player if
   * applicable.
   *
   * @param identifier Identifier of slot into which stack is equipped
   * @param stack      The ItemStack in question
   * @return {@link DropRule}
   */
  @Nonnull
  default DropRule getDropRule(LivingEntity livingEntity, ItemStack stack) {
	return defaultInstance.getDropRule(livingEntity);
  }

  /**
   * Determines whether or not Curios will automatically add tooltip listing
   * attribute modifiers that are returned by
   * {@link IItemCurio#getAttributeModifiers(String)}.
   *
   * @param identifier The identifier of the {@link ISlotType} of the slot
   * @param stack      The ItemStack in question
   * @return True to show attributes tooltip, false to disable
   */
  default boolean showAttributesTooltip(String identifier, ItemStack stack) {
	return defaultInstance.showAttributesTooltip(identifier);
  }

  /**
   * Allows to set the amount of bonus Fortune levels that are provided by curio.
   * Default implementation returns level of Fortune enchantment on ItemStack.
   *
   * @param identifier   The identifier of the {@link ISlotType} of the slot
   * @param curio        ItemStack that is checked
   * @param index        The index of the slot
   * @param livingEntity The LivingEntity that is wearing the ItemStack
   * @return Amount of additional Fortune levels that will be applied when mining
   */
  default int getFortuneBonus(String identifier, LivingEntity livingEntity, ItemStack curio, int index) {
	return defaultInstance.getFortuneBonus(identifier, livingEntity, curio, index);
  }

  /**
   * Allows to set the amount of bonus Looting levels that are provided by curio.
   * Default implementation returns level of Looting enchantment on ItemStack.
   *
   * @param identifier   The identifier of the {@link ISlotType} of the slot
   * @param curio        ItemStack that is checked
   * @param index        The index of the slot
   * @param livingEntity The LivingEntity that is wearing the ItemStack
   * @return Amount of additional Looting levels that will be applied in
   *         LootingLevelEvent
   */
  default int getLootingBonus(String identifier, LivingEntity livingEntity, ItemStack curio, int index) {
	return defaultInstance.getLootingBonus(identifier, livingEntity, curio, index);
  }

  /**
   * Determines if the ItemStack has rendering.
   *
   * @param identifier   The identifier of the {@link ISlotType} of the slot
   * @param index        The index of the slot
   * @param livingEntity The LivingEntity that is wearing the ItemStack
   * @param stack        The ItemStack in question
   * @return True if the ItemStack has rendering, false if it does not
   */
  default boolean canRender(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {
	return defaultInstance.canRender(identifier, index, livingEntity);
  }

  /**
   * Performs rendering of the ItemStack if
   * {@link IItemCurio#canRender(String, int, LivingEntity)} returns true. Note
   * that vertical sneaking translations are automatically applied before this
   * rendering method is called.
   *
   * @param identifier   The identifier of the {@link ISlotType} of the slot
   * @param livingEntity The LivingEntity that is wearing the ItemStack
   * @param stack        The ItemStack in question
   */
  default void render(String identifier, int index, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer,
	  int light, LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float partialTicks,
	  float ageInTicks, float netHeadYaw, float headPitch, ItemStack stack) {

	defaultInstance.render(identifier, index, matrixStack, renderTypeBuffer, light, livingEntity, limbSwing,
		limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
  }

}
