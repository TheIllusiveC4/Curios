/*
 * Copyright (C) 2018-2019  C4
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

package top.theillusivec4.curios.api.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class CurioStackHandler extends ItemStackHandler {

  protected NonNullList<ItemStack> previousStacks;
  protected boolean                isHidden = false;

  public CurioStackHandler() {

    this(1);
  }

  public CurioStackHandler(int size) {

    this.setSize(size);
  }

  public CurioStackHandler(NonNullList<ItemStack> stacks) {

    this.stacks = stacks;
    this.previousStacks = NonNullList.create();

    for (int i = 0; i < stacks.size(); i++) {
      previousStacks.add(ItemStack.EMPTY);
    }
  }

  @Override
  public void setSize(int size) {

    this.stacks = NonNullList.create();
    this.previousStacks = NonNullList.create();

    for (int i = 0; i < size; i++) {
      this.stacks.add(ItemStack.EMPTY);
      this.previousStacks.add(ItemStack.EMPTY);
    }
  }

  public void setPreviousStackInSlot(int slot, @Nonnull ItemStack stack) {

    validateSlotIndex(slot);
    this.previousStacks.set(slot, stack);
    onContentsChanged(slot);
  }

  public int getPreviousSlots() {

    return previousStacks.size();
  }

  @Nonnull
  public ItemStack getPreviousStackInSlot(int slot) {

    validateSlotIndex(slot);
    return this.previousStacks.get(slot);
  }

  public void addSize(int amount) {

    if (amount < 0) {
      throw new IllegalArgumentException("Amount cannot be negative!");
    }

    for (int i = 0; i < amount; i++) {
      this.stacks.add(ItemStack.EMPTY);
      this.previousStacks.add(ItemStack.EMPTY);
    }
  }

  public void removeSize(int amount) {

    if (amount < 0) {
      throw new IllegalArgumentException("Amount cannot be negative!");
    }
    int targetSize = this.stacks.size() - amount;

    while (this.stacks.size() > targetSize) {
      this.stacks.remove(this.stacks.size() - 1);
    }

    while (this.previousStacks.size() > targetSize) {
      this.previousStacks.remove(this.previousStacks.size() - 1);
    }
  }

  public boolean isHidden() {

    return isHidden;
  }

  public void setHidden(boolean hidden) {

    isHidden = hidden;
  }

  @Override
  public CompoundNBT serializeNBT() {

    CompoundNBT compound = super.serializeNBT();
    compound.putBoolean("Hidden", isHidden);
    return compound;
  }

  @Override
  public void deserializeNBT(CompoundNBT nbt) {

    this.isHidden = nbt.contains("Hidden", Constants.NBT.TAG_BYTE) && nbt.getBoolean("Hidden");
    super.deserializeNBT(nbt);
  }
}
