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

package top.theillusivec4.curios.api.type.capability;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.CuriosResources;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.type.ISlotType;

public interface ICurio {

  /**
   * Gets the ItemStack associated with the instance.
   *
   * @return The ItemStack associated with the instance
   */
  ItemStack getStack();

  /**
   * Called every tick on both client and server while the ItemStack is equipped.
   *
   * @param slotContext Context about the slot that the ItemStack is in
   */
  default void curioTick(SlotContext slotContext) {

  }

  /**
   * Called when the ItemStack is equipped into a slot or its data changes.
   *
   * @param slotContext Context about the slot that the ItemStack was just unequipped from
   * @param prevStack   The previous ItemStack in the slot
   */
  default void onEquip(SlotContext slotContext, ItemStack prevStack) {

  }


  /**
   * Called when the ItemStack changes its data in a slot.
   *
   * @param slotContent Context about the slot that the ItemStack is currently in
   * @param prevStack   A copy of the ItemStack in its previous state
   */
  default void onStateChange(SlotContext slotContent, ItemStack prevStack) {

  }

  /**
   * Called when the ItemStack is unequipped from a slot or its data changes.
   *
   * @param slotContext Context about the slot that the ItemStack was just unequipped from
   * @param newStack    The new ItemStack in the slot
   */
  default void onUnequip(SlotContext slotContext, ItemStack newStack) {

  }

  /**
   * Determines if the ItemStack can be equipped into a slot.
   *
   * @param slotContext Context about the slot that the ItemStack is attempting to equip into
   * @return True if the ItemStack can be equipped/put in, false if not
   */
  default boolean canEquip(SlotContext slotContext) {
    return true;
  }

  /**
   * Determines if the ItemStack can be unequipped from a slot.
   *
   * @param slotContext Context about the slot that the ItemStack is attempting to unequip from
   * @return True if the ItemStack can be unequipped/taken out, false if not
   */
  default boolean canUnequip(SlotContext slotContext) {
    return true;
  }

  /**
   * Retrieves a list of tooltips when displaying curio slot information. By default, this will be a
   * list of each slot identifier, translated and in gold text, associated with the curio.
   * <br>
   * If overriding, make sure the user has some indication which slots are associated with the
   * curio.
   *
   * @param tooltips A list of {@link Component} with every slot valid for this curio
   * @return A list of ITextComponent to display as curio slot information
   */
  default List<Component> getSlotsTooltip(List<Component> tooltips, Item.TooltipContext context) {
    return tooltips;
  }

  /**
   * Retrieves the default {@link CurioAttributeModifiers} for this curio item.
   *
   * <p>This is only active when a {@link CurioAttributeModifiers} data component does not already
   * exist on the item. For querying effective modifiers, instead use
   * {@link ICurioItem#getAttributeModifiers(ItemStack)}.
   *
   * @return The default curio attribute modifiers.
   */
  default CurioAttributeModifiers getDefaultCurioAttributeModifiers() {
    Map<String, ISlotType> slots = CuriosSlotTypes.getItemSlotTypes(this.getStack(), true);
    CurioAttributeModifiers.Builder builder = CurioAttributeModifiers.builder();

    for (String slot : slots.keySet()) {
      SlotContext slotContext = new SlotContext(slot, null, -1, false, true);
      this.getAttributeModifiers(slotContext, CuriosResources.resource(slot))
          .forEach((attributeHolder, attributeModifier) ->
                       builder.addModifier(attributeHolder, attributeModifier, slot));
    }
    return builder.build();
  }

  /**
   * Retrieves a map of attribute modifiers for the curio.
   * <br>
   * Note that only the identifier is guaranteed to be present in the slot context. For instances
   * where the ItemStack may not be in a curio slot, such as when retrieving item tooltips, the
   * index is -1 and the wearer may be null.
   *
   * @param slotContext Context about the slot that the ItemStack is in
   * @param id          Slot-unique id
   * @return A map of attribute modifiers to apply
   * @deprecated Use {@link #getDefaultCurioAttributeModifiers()} instead to create default
   *     attribute modifiers for a curio item.
   */
  @Deprecated(forRemoval = true)
  default Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
      SlotContext slotContext, ResourceLocation id) {
    return LinkedHashMultimap.create();
  }

  /**
   * Called server-side when the ItemStack is equipped by using it (i.e. from the hotbar), after
   * calling {@link ICurio#canEquipFromUse(SlotContext)}.
   * <br>
   * Default implementation plays the equip sound from {@link ICurio#getEquipSound(SlotContext)}.
   * This can be overridden to avoid that, but it is advised to always play something as an auditory
   * feedback for players.
   *
   * @param slotContext Context about the slot that the ItemStack was just equipped into
   */
  default void onEquipFromUse(SlotContext slotContext) {
    LivingEntity livingEntity = slotContext.entity();
    SoundInfo soundInfo = getEquipSound(slotContext);
    livingEntity.level().playSound(livingEntity,
                                   livingEntity.blockPosition(),
                                   soundInfo.soundEvent(),
                                   livingEntity.getSoundSource(),
                                   soundInfo.volume(),
                                   soundInfo.pitch());
  }

  /**
   * Retrieves the equip sound information for the given slot context.
   *
   * @param slotContext Context about the slot that the ItemStack was just equipped into
   * @return {@link SoundInfo} containing information about the sound event, volume, and pitch
   */
  @Nonnull
  default SoundInfo getEquipSound(SlotContext slotContext) {
    return new SoundInfo(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0f, 1.0f);
  }

  /**
   * Determines if the ItemStack can be automatically equipped into the first available slot when
   * used.
   *
   * @param slotContext Context about the slot that the ItemStack is attempting to equip into
   * @return True to enable auto-equipping when the item is used, false to disable
   */
  default boolean canEquipFromUse(SlotContext slotContext) {
    return false;
  }

  /**
   * Called when rendering break animations and sounds client-side when a worn curio item is
   * broken.
   *
   * @param slotContext Context about the slot that the ItemStack broke in
   */
  default void curioBreak(SlotContext slotContext) {
    playBreakAnimation(getStack(), slotContext.entity());
  }

  /**
   * Compares the current ItemStack and the previous ItemStack in the slot to detect any changes and
   * returns true if the change should be synced to all tracking clients. Note that this check
   * occurs every tick so implementations need to code their own timers for other intervals.
   *
   * @param slotContext Context about the slot that the ItemStack is in
   * @return True to sync the ItemStack change to all tracking clients, false to do nothing
   */
  default boolean canSync(SlotContext slotContext) {
    return false;
  }

  /**
   * Gets a tag that is used to sync extra curio data from the server to the client. Only used when
   * {@link ICurio#canSync(SlotContext)}  returns true.
   *
   * @param slotContext Context about the slot that the ItemStack is in
   * @return Data to be sent to the client
   */
  @Nonnull
  default CompoundTag writeSyncData(SlotContext slotContext) {
    return new CompoundTag();
  }

  /**
   * Used client-side to read data tags created by {@link ICurio#writeSyncData(SlotContext)} received from the
   * server.
   *
   * @param slotContext Context about the slot that the ItemStack is in
   * @param compound    Data received from the server
   */
  default void readSyncData(SlotContext slotContext, CompoundTag compound) {

  }

  /**
   * Determines if the ItemStack should drop on death and persist through respawn. This will persist
   * the ItemStack in the curio slot to the respawned player if applicable.
   *
   * @param slotContext Context about the slot that the ItemStack is attempting to equip into
   * @param source      The damage source that killed the wearer and triggered the drop
   * @param recentlyHit Whether the wearer was recently hit
   * @return The {@link DropRule} that applies to this curio
   */
  @Nonnull
  default DropRule getDropRule(SlotContext slotContext, DamageSource source, boolean recentlyHit) {
    return DropRule.DEFAULT;
  }

  /**
   * Retrieves a list of tooltips when displaying curio attribute modifier information returned by
   * {@link ICurio#getAttributeModifiers(SlotContext, ResourceLocation)}. By default, this will display a list
   * similar to the vanilla attribute modifier tooltips.
   *
   * @param tooltips A list of {@link Component} with the attribute modifier information
   * @param context  A context for retrieving tooltip data
   * @return A list of ITextComponent to display as curio attribute modifier information
   */
  default List<Component> getAttributesTooltip(List<Component> tooltips,
                                               Item.TooltipContext context) {
    return tooltips;
  }

  /**
   * Get the amount of bonus Fortune levels that are provided by curio.
   * Default implementation returns level of Fortune enchantment on ItemStack.
   *
   * @param slotContext Context about the slot that the ItemStack is in
   * @param lootContext Context for the loot drops
   * @return Amount of additional Fortune levels that will be applied when mining
   */
  default int getFortuneLevel(SlotContext slotContext, @Nullable LootContext lootContext) {
    return getStack().getEnchantmentLevel(
        slotContext.entity().level().holderLookup(Registries.ENCHANTMENT)
            .getOrThrow(Enchantments.FORTUNE));
  }

  /**
   * Get the amount of bonus Looting levels that are provided by curio.
   * Default implementation returns level of Looting enchantment on ItemStack.
   *
   * @param slotContext Context about the slot that the ItemStack is in
   * @param lootContext Context for the loot drops
   * @return Amount of additional Looting levels that will be applied in LootingLevelEvent
   */
  default int getLootingLevel(SlotContext slotContext, @Nullable LootContext lootContext) {
    return getStack().getEnchantmentLevel(
        slotContext.entity().level().holderLookup(Registries.ENCHANTMENT)
            .getOrThrow(Enchantments.LOOTING));
  }

  /**
   * Determines whether wearing the curio makes nearby piglins neutral, in the same manner as
   * wearing gold armor in vanilla.
   *
   * @param slotContext Context about the slot that the ItemStack is in
   * @return True if nearby piglins are neutral, false otherwise
   */
  default boolean makesPiglinsNeutral(SlotContext slotContext) {
    return getStack().makesPiglinsNeutral(slotContext.entity());
  }

  /**
   * Determines whether wearing the curio will allow the user to walk on powder snow, in the same manner as
   * wearing leather boots in vanilla.
   *
   * @param slotContext Context about the slot that the ItemStack is in
   * @return True if the user can walk on powder snow, false otherwise
   */
  default boolean canWalkOnPowderedSnow(SlotContext slotContext) {
    return getStack().canWalkOnPowderedSnow(slotContext.entity());
  }

  /**
   * Determines whether wearing the curio masks the user's eyes against Enderman, in the same manner
   * as wearing a pumpkin in vanilla.
   *
   * @param slotContext Context about the slot that the ItemStack is in
   * @param enderMan    The Enderman entity that the user is looking at
   * @return True if it can mask the user from Enderman, false otherwise
   */
  default boolean isEnderMask(SlotContext slotContext, EnderMan enderMan) {

    if (slotContext.entity() instanceof Player player) {
      return getStack().isGazeDisguise(player, enderMan);
    } else {
      return false;
    }
  }

  /*
   * Copy of vanilla implementation for breaking items client-side
   */
  static void playBreakAnimation(ItemStack stack, LivingEntity livingEntity) {
    if (!stack.isEmpty()) {
      if (!livingEntity.isSilent()) {
        livingEntity.level()
            .playLocalSound(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                            SoundEvents.ITEM_BREAK.value(), livingEntity.getSoundSource(), 0.8F,
                            0.8F + livingEntity.level().random.nextFloat() * 0.4F, false);
      }

      for (int i = 0; i < 5; ++i) {
        Vec3 vec3d = new Vec3((livingEntity.getRandom().nextFloat() - 0.5D) * 0.1D,
                              Math.random() * 0.1D + 0.1D, 0.0D);
        vec3d = vec3d.xRot(-livingEntity.getXRot() * ((float) Math.PI / 180F));
        vec3d = vec3d.yRot(-livingEntity.getYRot() * ((float) Math.PI / 180F));
        double d0 = (-livingEntity.getRandom().nextFloat()) * 0.6D - 0.3D;
        Vec3 vec3d1 = new Vec3((livingEntity.getRandom().nextFloat() - 0.5D) * 0.3D,
                               d0, 0.6D);
        vec3d1 = vec3d1.xRot(-livingEntity.getXRot() * ((float) Math.PI / 180F));
        vec3d1 = vec3d1.yRot(-livingEntity.getYRot() * ((float) Math.PI / 180F));
        vec3d1 = vec3d1.add(livingEntity.getX(),
                            livingEntity.getY() + livingEntity.getEyeHeight(), livingEntity.getZ());
        livingEntity.level()
            .addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), vec3d1.x, vec3d1.y,
                         vec3d1.z, vec3d.x, vec3d.y + 0.05D, vec3d.z);
      }
    }
  }

  record SoundInfo(SoundEvent soundEvent, float volume, float pitch) {

  }
}
