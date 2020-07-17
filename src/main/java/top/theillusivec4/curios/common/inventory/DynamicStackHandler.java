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

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.collection.DefaultedList;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.mixin.ISimpleInventoryAccessor;

public class DynamicStackHandler extends SimpleInventory implements IDynamicStackHandler {

  protected DefaultedList<ItemStack> previousStacks;

  public DynamicStackHandler(int size) {
    super(size);
    this.previousStacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
  }

  @Override
  public void setPreviousStack(int slot, ItemStack stack) {
    this.previousStacks.set(slot, stack);
  }

  @Override
  public ItemStack getPreviousStack(int slot) {
    return this.previousStacks.get(slot);
  }

  @Override
  public void grow(int amount) {
    ((ISimpleInventoryAccessor) this).setStacks(
        getResizedList(this.size() + amount, ((ISimpleInventoryAccessor) this).getStacks()));
    this.previousStacks = getResizedList(this.previousStacks.size() + amount, this.previousStacks);
    ((ISimpleInventoryAccessor) this).setSize(this.size() + amount);
  }

  @Override
  public void shrink(int amount) {
    ((ISimpleInventoryAccessor) this).setStacks(
        getResizedList(this.size() - amount, ((ISimpleInventoryAccessor) this).getStacks()));
    this.previousStacks = getResizedList(this.previousStacks.size() - amount, this.previousStacks);
    ((ISimpleInventoryAccessor) this).setSize(this.size() - amount);
  }

  @Override
  public CompoundTag serializeTag() {
    ListTag listTag = new ListTag();

    for (int i = 0; i < this.size(); ++i) {
      ItemStack itemStack = this.getStack(i);
      if (!itemStack.isEmpty()) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putByte("Slot", (byte) i);
        itemStack.toTag(compoundTag);
        listTag.add(compoundTag);
      }
    }
    CompoundTag tag = new CompoundTag();
    tag.put("Items", listTag);
    tag.putInt("Size", ((ISimpleInventoryAccessor) this).getStacks().size());
    return tag;
  }

  @Override
  public void deserializeTag(CompoundTag tag) {
    int size = tag.contains("Size", 3) ? tag.getInt("Size")
        : ((ISimpleInventoryAccessor) this).getStacks().size();
    ((ISimpleInventoryAccessor) this).setStacks(DefaultedList.ofSize(size, ItemStack.EMPTY));
    this.previousStacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
    ((ISimpleInventoryAccessor) this).setSize(size);

    if (tag.contains("Items")) {
      ListTag items = tag.getList("Items", 10);
      int j;

      for (j = 0; j < this.size(); ++j) {
        this.setStack(j, ItemStack.EMPTY);
      }

      for (j = 0; j < items.size(); ++j) {
        CompoundTag compoundTag = items.getCompound(j);
        int k = compoundTag.getByte("Slot") & 255;

        if (k < this.size()) {
          this.setStack(k, ItemStack.fromTag(compoundTag));
        }
      }
    }
  }

  private static DefaultedList<ItemStack> getResizedList(int size,
      DefaultedList<ItemStack> stacks) {
    DefaultedList<ItemStack> newList = DefaultedList.ofSize(size, ItemStack.EMPTY);

    for (int i = 0; i < newList.size() && i < stacks.size(); i++) {
      newList.set(i, stacks.get(i));
    }
    return newList;
  }
}
