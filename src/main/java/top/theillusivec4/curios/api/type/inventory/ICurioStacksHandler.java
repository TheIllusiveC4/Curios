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

package top.theillusivec4.curios.api.type.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;

public interface ICurioStacksHandler {

  /**
   * Gets the {@link IDynamicStackHandler} for the equipped curio stacks.
   *
   * @return The {@link IDynamicStackHandler} for the equipped curio stacks
   */
  IDynamicStackHandler getStacks();

  /**
   * Gets the {@link IDynamicStackHandler} for the equipped cosmetic curio stacks.
   * <br>
   * The size of this list should always match the sie of {@link ICurioStacksHandler#getStacks()}
   *
   * @return The {@link IDynamicStackHandler} for the equipped cosmetic curio stacks
   */
  IDynamicStackHandler getCosmeticStacks();

  /**
   * Gets a list of boolean values that represent render states. True for rendering and false for no
   * rendering.
   * <br>
   * The size of this list should always match the size of {@link ICurioStacksHandler#getStacks()}.
   *
   * @return A list of boolean values for render states
   */
  NonNullList<Boolean> getRenders();

  /**
   * Gets the number of slots for equipped curio stacks.
   * <br>
   * This number should always match the size of {@link ICurioStacksHandler#getStacks()}
   *
   * @return The number of slots for equipped curio stacks.
   */
  int getSlots();

  /**
   * Gets the size offset for this instance. This value is used to persist size changes for this
   * handler even when the underlying size changes.
   *
   * @return The number of the size offset for this instance
   */
  int getSizeShift();

  /**
   * Gets whether or not this stack handler should be visible. This does not lock the stack handler
   * from being used regardless.
   *
   * @return True or false for visibility
   */
  boolean isVisible();

  /**
   * Gets whether or not this stack handler has cosmetic handling. This does not lock the cosmetic
   * stack handler from being used regardless.
   *
   * @return True or false for cosmetic handling
   */
  boolean hasCosmetic();

  /**
   * Increases the number of slots by the given amount.
   *
   * @param amount The number of slots to add to the handler
   */
  void grow(int amount);

  /**
   * Decreases the number of slots by the given amount. This should not decrease the final number of
   * slots below 1.
   *
   * @param amount The number of slots to remove from the handler
   */
  void shrink(int amount);

  /**
   * Writes the data for this handler.
   *
   * @return A {@link CompoundTag} representing the serialized data
   */
  CompoundTag serializeNBT();

  /**
   * Reads the data into this handler.
   *
   * @param nbt A {@link CompoundTag} representing the serialized data
   */
  void deserializeNBT(CompoundTag nbt);
}
