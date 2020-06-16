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

package top.theillusivec4.curios.api.inventory;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

public final class CurioSlotStackHandler extends ItemStackHandler {

  private NonNullList<ExtraStackHandler> extraStacks;

  public CurioSlotStackHandler() {
    this(1);
  }

  public CurioSlotStackHandler(int size) {
    this.setSize(size);
  }

  @Override
  public void setSize(int size) {
    this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    this.extraStacks = NonNullList.withSize(size, new ExtraStackHandler());
  }

  public void setPreviousStackInSlot(int slot, @Nonnull ItemStack stack) {
    validateSlotIndex(slot);
    this.extraStacks.get(slot).setPreviousStack(stack);
    onContentsChanged(slot);
  }

  @Nonnull
  public ItemStack getPreviousStackInSlot(int slot) {
    validateSlotIndex(slot);
    return this.extraStacks.get(slot).getPreviousStack();
  }

  public void setCosmeticStackInSlot(int slot, @Nonnull ItemStack stack) {
    validateSlotIndex(slot);
    this.extraStacks.get(slot).setCosmeticStack(stack);
    onContentsChanged(slot);
  }

  @Nonnull
  public ItemStack getCosmeticStackInSlot(int slot) {
    validateSlotIndex(slot);
    return this.extraStacks.get(slot).getCosmeticStack();
  }

  public void increaseSize(int amount) {

    if (amount < 0) {
      throw new IllegalArgumentException("Amount cannot be negative!");
    }

    for (int i = 0; i < amount; i++) {
      this.stacks.add(ItemStack.EMPTY);
      this.extraStacks.add(new ExtraStackHandler());
    }
  }

  public void decreaseSize(int amount) {

    if (amount < 0) {
      throw new IllegalArgumentException("Amount cannot be negative!");
    }
    int targetSize = this.stacks.size() - amount;

    while (this.stacks.size() > targetSize) {
      this.stacks.remove(this.stacks.size() - 1);
    }

    while (this.extraStacks.size() > targetSize) {
      this.extraStacks.remove(this.extraStacks.size() - 1);
    }
  }

  @Override
  public CompoundNBT serializeNBT() {
    CompoundNBT compound = super.serializeNBT();
    ListNBT nbtTagList = new ListNBT();

    for (int i = 0; i < this.extraStacks.size(); i++) {
      CompoundNBT extrasTag = new CompoundNBT();
      extrasTag.putInt("Slot", i);
      CompoundNBT cosmeticTag = new CompoundNBT();
      this.extraStacks.get(i).getCosmeticStack().write(cosmeticTag);
      extrasTag.put("Cosmetic", cosmeticTag);
      extrasTag.putBoolean("Render", this.extraStacks.get(i).canRender());
      nbtTagList.add(extrasTag);
    }
    compound.put("ExtraItems", nbtTagList);
    return compound;
  }

  @Override
  public void deserializeNBT(CompoundNBT nbt) {
    super.deserializeNBT(nbt);

    if (nbt.contains("ExtraItems")) {
      ListNBT tagList = nbt.getList("ExtraItems", Constants.NBT.TAG_COMPOUND);

      for (int i = 0; i < tagList.size(); i++) {
        CompoundNBT extrasTag = tagList.getCompound(i);
        int slot = extrasTag.getInt("Slot");

        if (slot >= 0 && slot < this.extraStacks.size()) {
          CompoundNBT cosmeticTag = extrasTag.getCompound("Cosmetic");
          ItemStack cosmeticStack = ItemStack.read(cosmeticTag);
          this.extraStacks
              .set(slot, new ExtraStackHandler(cosmeticStack, extrasTag.getBoolean("Render")));
        }
      }
    }
  }

  public static class ExtraStackHandler {

    private ItemStack cosmeticStack = ItemStack.EMPTY;
    private ItemStack previousStack = ItemStack.EMPTY;
    private boolean render = false;

    public ExtraStackHandler() {
    }

    public ExtraStackHandler(ItemStack cosmeticStack, boolean render) {
      this.cosmeticStack = cosmeticStack;
      this.render = render;
    }

    public ItemStack getCosmeticStack() {
      return cosmeticStack;
    }

    public void setCosmeticStack(ItemStack cosmeticStack) {
      this.cosmeticStack = cosmeticStack;
    }

    public ItemStack getPreviousStack() {
      return previousStack;
    }

    public void setPreviousStack(ItemStack previousStack) {
      this.previousStack = previousStack;
    }

    public boolean canRender() {
      return render;
    }

    public void setRender(boolean render) {
      this.render = render;
    }
  }
}
