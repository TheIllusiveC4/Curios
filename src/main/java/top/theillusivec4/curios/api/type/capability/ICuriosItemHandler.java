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

import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public interface ICuriosItemHandler {

  Logger LOGGER = LogUtils.getLogger();

  /**
   * A map of the current curios, keyed by the {@link ISlotType} identifier.
   *
   * @return The current curios equipped
   */
  Map<String, ICurioStacksHandler> getCurios();

  /**
   * Sets the current curios map to the one passed in.
   *
   * @param map The curios collection that will replace the current one
   */
  void setCurios(Map<String, ICurioStacksHandler> map);

  /**
   * Gets the number of slots across all {@link ISlotType} identifiers.
   *
   * @return The number of slots
   */
  int getSlots();

  /**
   * Gets the number of visible slots across all {@link ISlotType} identifiers.
   *
   * @return The number of visible slots
   */
  default int getVisibleSlots() {
    return this.getSlots();
  }

  /**
   * Resets the current curios map to default values.
   */
  void reset();

  /**
   * Gets the Optional {@link ICurioStacksHandler} associated with the given {@link ISlotType}
   * identifier or Optional.empty() if it doesn't exist.
   *
   * @param identifier The identifier for the {@link ISlotType}
   * @return The stack handler
   */
  Optional<ICurioStacksHandler> getStacksHandler(String identifier);

  /**
   * Gets a {@link LazyOptional} of an {@link IItemHandlerModifiable} that contains all the
   * equipped curio stacks (not including cosmetics).
   *
   * @return The equipped curio stacks, or empty if there is no curios handler
   */
  IItemHandlerModifiable getEquippedCurios();

  /**
   * Replaces the currently equipped item in a specified curio slot, if it exists.
   *
   * @param identifier The identifier of the curio slot
   * @param index      The index of the curio slot
   * @param stack      The new stack to place into the slot
   */
  void setEquippedCurio(String identifier, int index, ItemStack stack);

  /**
   * Checks if the given item is equipped in a curio slot.
   *
   * @param item The item to search for
   * @return True if the item is equipped in a curio slot, false otherwise
   */
  default boolean isEquipped(Item item) {
    return this.findFirstCurio(item).isPresent();
  }

  /**
   * Checks if an item that matches the given filter is equipped in a curio slot.
   *
   * @param filter The filter to test against
   * @return True if the item is equipped in a curio slot, false otherwise
   */
  default boolean isEquipped(Predicate<ItemStack> filter) {
    return this.findFirstCurio(filter).isPresent();
  }

  /**
   * Gets the first matching item equipped in a curio slot.
   *
   * @param item The item to search for
   * @return An optional {@link SlotResult} with the found item, or empty if none were found
   */
  Optional<SlotResult> findFirstCurio(Item item);

  /**
   * Gets the first matching item equipped in a curio slot that matches the filter.
   *
   * @param filter The filter to test against
   * @return An optional {@link SlotResult} with the found item, or empty if none were found
   */
  Optional<SlotResult> findFirstCurio(Predicate<ItemStack> filter);

  /**
   * Gets all matching items equipped in a curio slot.
   *
   * @param item The item to search for
   * @return A list of matching results
   */
  List<SlotResult> findCurios(Item item);

  /**
   * Gets all matching items equipped in a curio slot that matches the filter.
   *
   * @param filter The filter to test against
   * @return A list of matching results
   */
  List<SlotResult> findCurios(Predicate<ItemStack> filter);

  /**
   * Gets all items equipped in all curio slots with specific identifiers.
   *
   * @param identifiers The identifiers for the slot types
   * @return A list of matching results
   */
  List<SlotResult> findCurios(String... identifiers);

  /**
   * Gets the currently equipped item in a specified curio slot, if it exists.
   *
   * @param identifier The identifier of the curio slot
   * @param index      The index of the curio slot
   * @return The equipped curio stack, or empty if there is none
   */
  Optional<SlotResult> findCurio(String identifier, int index);

  /**
   * Gets the wearer/owner of this handler instance.
   *
   * @return The wearer
   */
  LivingEntity getWearer();

  /**
   * Adds an ItemStack to the invalid cache. Used for storing items found in the process of
   * disabling/removing a non-empty slot.
   *
   * @param stack The ItemStack to add
   */
  void loseInvalidStack(ItemStack stack);

  /**
   * Drops all the ItemStacks found in the invalid stacks list. Used for handling items found in
   * disabling/removing slots.
   */
  void handleInvalidStacks();

  /**
   * Get the amount of Fortune levels that are provided by curios.
   */
  int getFortuneLevel(@Nullable LootContext lootContext);

  /**
   * Get the amount of Looting levels that are provided by curios.
   */
  int getLootingLevel(DamageSource source, LivingEntity target, int baseLooting);

  /**
   * Saves the curios inventory stacks to NBT.
   *
   * @param clear True to clear the inventory while saving, false to just save the data
   * @return {@link ListTag} with the curios inventory stacks data
   */
  ListTag saveInventory(boolean clear);

  /**
   * Saves the curios inventory stacks that pass a filter to NBT. Only stacks that pass the filter
   * will be cleared if clear is true.
   *
   * @param clear True to clear the inventory while saving, false to just save the data
   * @param filter The filter that will test for each stack found in the curios inventory to save
   * @return {@link ListTag} with the curios inventory stacks data
   */
  default ListTag saveInventory(boolean clear, Predicate<ItemStack> filter) {
    return this.saveInventory(clear);
  }

  /**
   * Saves the curios inventory stacks that pass a filter to NBT. Only stacks that pass the filter
   * will be cleared if clear is true.
   *
   * @param clear True to clear the inventory while saving, false to just save the data
   * @param filter The filter that will test for each stack found in the curios inventory to save
   * @return {@link ListTag} with the curios inventory stacks data
   */
  default ListTag saveInventory(boolean clear, BiPredicate<ItemStack, SlotContext> filter) {
    return this.saveInventory(clear);
  }

  /**
   * Loads the curios inventory stacks from NBT.
   *
   * @param data {@link ListTag} data from {@link ICuriosItemHandler#saveInventory(boolean)}
   */
  void loadInventory(ListTag data);

  /**
   * Retrieves a set containing the {@link ICurioStacksHandler} that require its slot modifiers be
   * synced to tracking clients.
   *
   * @return A set of {@link ICurioStacksHandler} that need to be synced to tracking clients
   */
  Set<ICurioStacksHandler> getUpdatingInventories();


  /**
   * Adds the specified slot modifier to the handler as temporary slot modifiers.
   * <br>
   * These slot modifiers are not serialized and disappear upon deserialization.
   *
   * @param slot      Identifier of the {@link ISlotType} to add the slot modifier to
   * @param uuid      UUID for the {@link AttributeModifier}
   * @param name      Name for the attribute modifier
   * @param amount    Amount for the attribute modifier
   * @param operation Operation for the attribute modifier
   */
  default void addTransientSlotModifier(String slot, UUID uuid, String name, double amount,
                                        AttributeModifier.Operation operation) {
    LOGGER.error("Missing method implementation!");
  }

  /**
   * Adds the specified slot modifiers to the handler as temporary slot modifiers.
   * <br>
   * These slot modifiers are not serialized and disappear upon deserialization.
   *
   * @param modifiers A {@link Multimap} with slot identifiers as keys and attribute modifiers as values
   */
  void addTransientSlotModifiers(Multimap<String, AttributeModifier> modifiers);

  /**
   * Adds the specified slot modifier to the handler as temporary slot modifiers.
   * <br>
   * These slot modifiers are not serialized and disappear upon deserialization.
   *
   * @param slot      Identifier of the {@link ISlotType} to add the slot modifier to
   * @param uuid      UUID for the {@link AttributeModifier}
   * @param name      Name for the attribute modifier
   * @param amount    Amount for the attribute modifier
   * @param operation Operation for the attribute modifier
   */
  default void addPermanentSlotModifier(String slot, UUID uuid, String name, double amount,
                                        AttributeModifier.Operation operation) {
    LOGGER.error("Missing method implementation!");
  }

  /**
   * Adds the specified slot modifiers to the handler as permanent slot modifiers.
   *
   * @param modifiers A {@link Multimap} with slot identifiers as keys and attribute modifiers as values
   */
  void addPermanentSlotModifiers(Multimap<String, AttributeModifier> modifiers);

  /**
   * Removes the specified slot modifier (via UUID) from the handler.
   *
   * @param slot Identifier of the {@link ISlotType} to remove the modifier from
   * @param uuid UUID of the {@link AttributeModifier} to remove
   */
  default void removeSlotModifier(String slot, UUID uuid) {
    LOGGER.error("Missing method implementation!");
  }

  /**
   * Removes the specified slot modifiers from the handler.
   *
   * @param modifiers A {@link Multimap} with slot identifiers as keys and attribute modifiers as values
   */
  void removeSlotModifiers(Multimap<String, AttributeModifier> modifiers);

  /**
   * Removes all the slot modifiers from the handler.
   */
  void clearSlotModifiers();

  /**
   * Retrieves all the slot modifiers from the handler.
   *
   * @return A {@link Multimap} with slot identifiers as keys and attribute modifiers as values
   */
  Multimap<String, AttributeModifier> getModifiers();

  /**
   * Serializes the curios inventory data
   */
  Tag writeTag();

  /**
   * Deserializes the curios inventory data
   */
  void readTag(Tag tag);

  /**
   * Removes the cached modifiers that appear upon deserialization of the handler.
   * <br>
   * Primarily for internal use, used as a workaround to avoid calculating slot stacks before slot
   * modifiers are initially applied.
   */
  void clearCachedSlotModifiers();

  // =============== DEPRECATED =================

  /**
   * @deprecated Lock states are no longer used
   */
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  default Set<String> getLockedSlots() {
    return new HashSet<>();
  }

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#addTransientSlotModifiers(Multimap)}
   * and {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#addPermanentSlotModifiers(Multimap)}
   */
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  default void unlockSlotType(String identifier, int amount, boolean visible, boolean cosmetic) {
    growSlotType(identifier, amount);
  }

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#addTransientSlotModifiers(Multimap)}
   * and {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#addPermanentSlotModifiers(Multimap)}
   */
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  default void lockSlotType(String identifier) {
    shrinkSlotType(identifier, 1);
  }

  /**
   * @deprecated Lock states are no longer used
   */
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  default void processSlots() {
    // NO-OP
  }

  /**
   * @deprecated See {@link ICuriosItemHandler#getFortuneLevel(LootContext)}
   */
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  default int getFortuneBonus() {
    return 0;
  }

  /**
   * @deprecated See {@link ICuriosItemHandler#getLootingLevel(DamageSource, LivingEntity, int)}
   */
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  default int getLootingBonus() {
    return 0;
  }


  /**
   * @deprecated See {@link ICuriosItemHandler#getLootingLevel(DamageSource, LivingEntity, int)} and
   * {@link ICuriosItemHandler#getFortuneLevel(LootContext)}
   */
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  default void setEnchantmentBonuses(Tuple<Integer, Integer> fortuneAndLooting) {
    // NO-OP
  }

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#addTransientSlotModifiers(Multimap)}
   * and {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#addPermanentSlotModifiers(Multimap)}
   */
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  void growSlotType(String identifier, int amount);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#addTransientSlotModifiers(Multimap)}
   * and {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#addPermanentSlotModifiers(Multimap)}
   */
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  void shrinkSlotType(String identifier, int amount);
}
