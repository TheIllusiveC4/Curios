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

package top.theillusivec4.curios.api.type.inventory;

import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public interface IDynamicStackHandler extends ResourceHandler<ItemResource>, ValueIOSerializable {

  /**
   * Sets a {@link ItemStack} to the given slot index as the current stack.
   *
   * @param slot  The slot index
   * @param stack The {@link ItemStack} to assign as the current stack
   */
  void setStackInSlot(int slot, @Nonnull ItemStack stack);

  /**
   * Gets the {@link ItemStack} assigned as the current stack in the given slot index.
   *
   * @param slot The slot index
   * @return The {@link ItemStack} assigned as the current stack
   */
  @Nonnull
  ItemStack getStackInSlot(int slot);

  /**
   * Sets a {@link ItemStack} to the given slot index as the previous stack, for comparison purposes
   * with the current stack.
   *
   * @param slot  The slot index
   * @param stack The {@link ItemStack} to assign as the previous stack
   */
  void setPreviousStackInSlot(int slot, @Nonnull ItemStack stack);

  /**
   * Gets the {@link ItemStack} assigned as the previous stack in the given slot index.
   *
   * @param slot The slot index
   * @return The {@link ItemStack} assigned as the previous stack
   */
  ItemStack getPreviousStackInSlot(int slot);

  /**
   * @return The total number of slots.
   */
  int getSlots();

  /**
   * Increases the number of slots by the given amount.
   *
   * @param amount The number of slots to add.
   */
  void grow(int amount);

  /**
   * Decreases the number of slots by the given amount.
   *
   * @param amount The number of slots to remove
   */
  void shrink(int amount);

  /**
   * @deprecated Use {@link ResourceHandler#isValid(int, Resource)} instead.
   */
  @Deprecated(since = "17.0.0", forRemoval = true)
  default boolean isItemValid(int slot, ItemStack stack) {
    return this.isValid(slot, ItemResource.of(stack));
  }

  /**
   * @deprecated Use {@link ResourceHandler#getCapacityAsInt(int, Resource)} instead.
   */
  @Deprecated(since = "17.0.0", forRemoval = true)
  default int getSlotLimit(int slot) {
    return this.getCapacityAsInt(slot, ItemResource.EMPTY);
  }

  /**
   * @deprecated Use {@link ResourceHandler#insert} instead.
   */
  @Deprecated(since = "17.0.0", forRemoval = true)
  default ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    ItemResource resource = ItemResource.of(stack);
    int toInsert = stack.getCount();

    if (simulate) {
      int space = this.getCapacityAsInt(slot, resource) - this.getAmountAsInt(slot);
      return space >= toInsert ? ItemStack.EMPTY : stack.copyWithCount(toInsert - space);
    }
    int inserted = this.insert(slot, resource, stack.getMaxStackSize(), Transaction.open(null));
    return inserted >= toInsert ? ItemStack.EMPTY : stack.copyWithCount(toInsert - inserted);
  }

  /**
   * @deprecated Use {@link ResourceHandler#extract} instead.
   */
  @Deprecated(since = "17.0.0", forRemoval = true)
  default ItemStack extractItem(int slot, int amount, boolean simulate) {
    int existingAmount = this.getAmountAsInt(slot);
    int extracted = Math.min(existingAmount, amount);
    ItemResource resource = this.getResource(slot);

    if (!simulate) {
      extracted =
          this.extract(slot, this.getResource(slot), existingAmount, Transaction.open(null));
    }
    return extracted > 0 ? resource.toStack(extracted) : ItemStack.EMPTY;
  }
}
