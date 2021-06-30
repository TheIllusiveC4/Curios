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

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
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
   * Gets the an Optional {@link ICurioStacksHandler} associated with the given {@link ISlotType}
   * identifier or Optional.empty() if it doesn't exist.
   *
   * @param identifier The identifier for the {@link ISlotType}
   * @return The stack handler
   */
  Optional<ICurioStacksHandler> getStacksHandler(String identifier);

  /**
   * Adds an amount of slots to the {@link ICurioStacksHandler} of a {@link ISlotType} associated
   * with the identifier.
   *
   * @param identifier The identifier for the {@link ISlotType}
   * @param amount     The number of slots to add, must be non-negative
   */
  void growSlotType(String identifier, int amount);

  /**
   * Removes an amount of slots from the {@link ICurioStacksHandler} of a {@link ISlotType}
   * associated with the identifier.
   *
   * @param identifier The identifier for the {@link ISlotType}
   * @param amount     The number of slots to remove, must be non-negative
   */
  void shrinkSlotType(String identifier, int amount);

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
   * Drops all of the ItemStacks found in the invalid stacks list. Used for handling items found in
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
   * Sets the total Fotrune and Looting bonuses of this handler and therefore it's bearer.
   */
  void setEnchantmentBonuses(Tuple<Integer, Integer> fortuneAndLooting);

  // =============== DEPRECATED =================

  /**
   * @deprecated Locked slots no longer exist
   */
  @Deprecated
  default Set<String> getLockedSlots() {
    return new HashSet<>();
  }

  /**
   * @deprecated Unlock slots by using {@link ICuriosItemHandler#growSlotType(String, int)}
   */
  @Deprecated
  default void unlockSlotType(String identifier, int amount, boolean visible, boolean cosmetic) {
    growSlotType(identifier, amount);
  }

  /**
   * @deprecated Lock slots by using {@link ICuriosItemHandler#shrinkSlotType(String, int)}
   */
  @Deprecated
  default void lockSlotType(String identifier) {
    shrinkSlotType(identifier, 1);
  }

  /**
   * @deprecated Lock states are no longer used
   */
  @Deprecated
  default void processSlots() {
    // NO-OP
  }
}
