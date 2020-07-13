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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.collection.DefaultedList;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CurioStacksHandler implements ICurioStacksHandler {

  private IDynamicStackHandler stackHandler;
  private IDynamicStackHandler cosmeticStackHandler;
  private int sizeShift;
  private boolean visible;
  private boolean cosmetic;
  private DefaultedList<Boolean> renderHandler;

  public CurioStacksHandler() {
    this(1, 0, true, false);
  }

  public CurioStacksHandler(int size, int shift, boolean visible, boolean cosmetic) {
    this.setSize(size + shift);
    this.sizeShift = shift;
    this.visible = visible;
    this.cosmetic = cosmetic;
  }

  public void setSize(int size) {
    this.stackHandler = new DynamicStackHandler(size);
    this.cosmeticStackHandler = new DynamicStackHandler(size);
    this.renderHandler = DefaultedList.ofSize(size, true);
    this.sizeShift = 0;
  }

  @Override
  public IDynamicStackHandler getStacks() {
    return this.stackHandler;
  }

  @Override
  public IDynamicStackHandler getCosmeticStacks() {
    return this.cosmeticStackHandler;
  }

  @Override
  public DefaultedList<Boolean> getRenders() {
    return this.renderHandler;
  }

  @Override
  public int getSlots() {
    return this.stackHandler.size();
  }

  @Override
  public int getSizeShift() {
    return this.sizeShift;
  }

  @Override
  public boolean isVisible() {
    return this.visible;
  }

  @Override
  public boolean hasCosmetic() {
    return this.cosmetic;
  }

  @Override
  public void grow(int amount) {
    this.validateSizeChange(amount);
    this.stackHandler.grow(amount);
    this.cosmeticStackHandler.grow(amount);
    DefaultedList<Boolean> newList = DefaultedList.ofSize(this.renderHandler.size() + amount, true);

    for (int i = 0; i < newList.size() && i < this.renderHandler.size(); i++) {
      newList.set(i, renderHandler.get(i));
    }
    this.renderHandler = newList;
    this.sizeShift += amount;
  }

  @Override
  public void shrink(int amount) {
    this.validateSizeChange(amount);
    amount = Math.min(this.stackHandler.size() - 1, amount);
    this.stackHandler.shrink(amount);
    this.cosmeticStackHandler.shrink(amount);
    DefaultedList<Boolean> newList = DefaultedList.ofSize(this.renderHandler.size() - amount, true);

    for (int i = 0; i < newList.size() && i < this.renderHandler.size(); i++) {
      newList.set(i, renderHandler.get(i));
    }
    this.renderHandler = newList;
    this.sizeShift -= amount;
  }

  private void validateSizeChange(int amount) {

    if (amount < 0) {
      throw new IllegalArgumentException("Amount cannot be negative!");
    }
  }

  @Override
  public CompoundTag serializeTag() {
    CompoundTag compoundNBT = new CompoundTag();
    compoundNBT.put("Stacks", this.stackHandler.serializeTag());
    compoundNBT.put("Cosmetics", this.cosmeticStackHandler.serializeTag());

    ListTag nbtTagList = new ListTag();

    for (int i = 0; i < this.renderHandler.size(); i++) {
      CompoundTag tag = new CompoundTag();
      tag.putInt("Slot", i);
      tag.putBoolean("Render", this.renderHandler.get(i));
      nbtTagList.add(tag);
    }
    CompoundTag nbt = new CompoundTag();
    nbt.put("Renders", nbtTagList);
    nbt.putInt("Size", this.renderHandler.size());
    compoundNBT.put("Renders", nbt);
    compoundNBT.putInt("SizeShift", this.sizeShift);
    compoundNBT.putBoolean("HasCosmetic", this.cosmetic);
    compoundNBT.putBoolean("Visible", this.visible);
    return compoundNBT;
  }

  @Override
  public void deserializeTag(CompoundTag tag) {

    if (tag.contains("Stacks")) {
      this.stackHandler.deserializeTag(tag.getCompound("Stacks"));
    }

    if (tag.contains("Cosmetics")) {
      this.cosmeticStackHandler.deserializeTag(tag.getCompound("Cosmetics"));
    }

    if (tag.contains("Renders")) {
      CompoundTag tag1 = tag.getCompound("Renders");
      this.renderHandler = DefaultedList
          .ofSize(tag1.contains("Size", 3) ? tag1.getInt("Size") : this.stackHandler.size(), true);
      ListTag tagList = tag1.getList("Renders", 10);

      for (int i = 0; i < tagList.size(); i++) {
        CompoundTag tags = tagList.getCompound(i);
        int slot = tags.getInt("Slot");

        if (slot >= 0 && slot < this.renderHandler.size()) {
          this.renderHandler.set(slot, tags.getBoolean("Render"));
        }
      }
    }

    if (tag.contains("SizeShift")) {
      this.sizeShift = tag.getInt("SizeShift");
    }
    this.cosmetic = tag.contains("HasCosmetic") ? tag.getBoolean("HasCosmetic") : this.cosmetic;
    this.visible = tag.contains("Visible") ? tag.getBoolean("Visible") : this.visible;
  }
}
