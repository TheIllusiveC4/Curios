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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Tuple;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public interface ICuriosItemHandler {

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

  default int getVisibleSlots() {
    return this.getSlots();
  }

  /**
   * Gets the identifiers of slot types locked for this handler.
   *
   * @return A set of {@link ISlotType} identifiers
   */
  Set<String> getLockedSlots();

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
   * Enables the {@link ISlotType} for a given identifier, adding the default settings to the curio
   * map.
   *
   * @param identifier The identifier for the {@link ISlotType}
   * @param amount     The amount of slots to unlock
   */
  void unlockSlotType(String identifier, int amount, boolean visible, boolean cosmetic);

  /**
   * Disables the {@link ISlotType} for a given identifier, removing it from the curio map.
   *
   * @param identifier The identifier for the {@link ISlotType}
   */
  void lockSlotType(String identifier);

  /**
   * Processes the lock/unlock slot states that are enqueued
   */
  void processSlots();

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
   * Returns the total Fortune bonus of all equipped curios.
   * Recalculated with each LivingUpdateEvent.
   */
  int getFortuneBonus();

  /**
   * Returns the total Looting bonus of all equipped curios.
   * Recalculated with each LivingUpdateEvent.
   */
  int getLootingBonus();

  /**
   * Saves the curios inventory stacks to NBT.
   *
   * @param clear True to clear the inventory while saving, false to just save the data
   * @return {@link ListNBT} with the curios inventory stacks data
   */
  ListNBT saveInventory(boolean clear);

  /**
   * Loads the curios inventory stacks from NBT.
   *
   * @param data {@link ListNBT} data from {@link ICuriosItemHandler#saveInventory(boolean)}
   */
  void loadInventory(ListNBT data);

  /**
   * Sets the total Fortune and Looting bonuses associated with the handler.
   *
   * @param fortuneAndLooting Two integers, the first one representing fortune and the second looting
   */
  void setEnchantmentBonuses(Tuple<Integer, Integer> fortuneAndLooting);

  /**
   * Retrieves a set containing the {@link ICurioStacksHandler} that require its slot modifiers be
   * synced to tracking clients.
   *
   * @return A set of {@link ICurioStacksHandler} that need to be synced to tracking clients
   */
  Set<ICurioStacksHandler> getUpdatingInventories();

  /**
   * Adds the specified slot modifiers to the handler as temporary slot modifiers.
   * <br>
   * These slot modifiers are not serialized and disappear upon deserialization.
   *
   * @param modifiers A {@link Multimap} with slot identifiers as keys and attribute modifiers as values
   */
  void addTransientSlotModifiers(Multimap<String, AttributeModifier> modifiers);

  /**
   * Adds the specified slot modifiers to the handler as permanent slot modifiers.
   *
   * @param modifiers A {@link Multimap} with slot identifiers as keys and attribute modifiers as values
   */
  void addPermanentSlotModifiers(Multimap<String, AttributeModifier> modifiers);

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
   * Serializes the handler to NBT.
   *
   * @return Data for the handler represented as a {@link CompoundNBT}
   */
  CompoundNBT writeNBT();

  /**
   * Deserializes the handler from NBT.
   *
   * @param compoundNBT Data for the handler represented as a {@link CompoundNBT}
   */
  void readNBT(CompoundNBT compoundNBT);

  /**
   * Removes the cached modifiers that appear upon deserialization of the handler.
   * <br>
   * Primarily for internal use, used as a workaround to avoid calculating slot stacks before slot
   * modifiers are initially applied.
   */
  void clearCachedSlotModifiers();

  // ============ DEPRECATED ================

  /**
   * @param identifier The identifier for the {@link ISlotType}
   * @param amount     The number of slots to add, must be non-negative
   * @deprecated Add a slot modifier instead using {@link top.theillusivec4.curios.api.type.util.ICuriosHelper#addSlotModifier(Multimap, String, UUID, double, AttributeModifier.Operation)}
   * when overriding {@link ICurio#getAttributeModifiers(SlotContext, UUID)}
   * <br>
   * Adds an amount of slots to the {@link ICurioStacksHandler} of a {@link ISlotType} associated
   * with the identifier.
   */
  @Deprecated
  void growSlotType(String identifier, int amount);

  /**
   * @param identifier The identifier for the {@link ISlotType}
   * @param amount     The number of slots to remove, must be non-negative
   * @deprecated Add a slot modifier instead using {@link top.theillusivec4.curios.api.type.util.ICuriosHelper#addSlotModifier(Multimap, String, UUID, double, AttributeModifier.Operation)}
   * when overriding {@link ICurio#getAttributeModifiers(SlotContext, UUID)}
   * <p>
   * Removes an amount of slots from the {@link ICurioStacksHandler} of a {@link ISlotType}
   * associated with the identifier.
   */
  @Deprecated
  void shrinkSlotType(String identifier, int amount);

}
