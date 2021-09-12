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

package top.theillusivec4.curios.common.inventory;

import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class DynamicStackHandler extends ItemStackHandler implements IDynamicStackHandler {

  protected NonNullList<ItemStack> previousStacks;

  public DynamicStackHandler(int size) {
    super(size);
    this.previousStacks = NonNullList.withSize(size, ItemStack.EMPTY);
  }

  @Override
  public void setPreviousStackInSlot(int slot, @Nonnull ItemStack stack) {
    this.validateSlotIndex(slot);
    this.previousStacks.set(slot, stack);
    this.onContentsChanged(slot);
  }

  @Override
  public int getSlots() {
    return stacks.size();
  }

  @Nonnull
  @Override
  public ItemStack getPreviousStackInSlot(int slot) {
    this.validateSlotIndex(slot);
    return this.previousStacks.get(slot);
  }

  @Override
  public void grow(int amount) {
    this.stacks = getResizedList(this.stacks.size() + amount, this.stacks);
    this.previousStacks = getResizedList(this.previousStacks.size() + amount, this.previousStacks);
  }

  @Override
  public void shrink(int amount) {
    this.stacks = getResizedList(this.stacks.size() - amount, this.stacks);
    this.previousStacks = getResizedList(this.previousStacks.size() - amount, this.previousStacks);
  }

  private static NonNullList<ItemStack> getResizedList(int size, NonNullList<ItemStack> stacks) {
    NonNullList<ItemStack> newList = NonNullList.withSize(Math.max(0, size), ItemStack.EMPTY);

    for (int i = 0; i < newList.size() && i < stacks.size(); i++) {
      newList.set(i, stacks.get(i));
    }
    return newList;
  }
}
