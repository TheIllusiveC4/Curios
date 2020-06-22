package top.theillusivec4.curios.api.inventory;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

public class CurioStacksHandler {

  private DynamicStackHandler stackHandler;
  private DynamicStackHandler cosmeticStackHandler;
  private int sizeShift = 0;

  private NonNullList<Boolean> renderHandler;

  public CurioStacksHandler() {
    this(1);
  }

  public CurioStacksHandler(int size) {
    this.setSize(size);
  }

  public void setSize(int size) {
    this.stackHandler = new DynamicStackHandler(size);
    this.cosmeticStackHandler = new DynamicStackHandler(size);
    this.renderHandler = NonNullList.withSize(size, true);
    this.sizeShift = 0;
  }

  public DynamicStackHandler getStacks() {
    return this.stackHandler;
  }

  public DynamicStackHandler getCosmeticStacks() {
    return this.cosmeticStackHandler;
  }

  public NonNullList<Boolean> getRenders() {
    return this.renderHandler;
  }

  public int getSlots() {
    return this.stackHandler.getSlots();
  }

  public int getSizeShift() {
    return this.sizeShift;
  }

  public void grow(int amount) {
    this.validateSizeChange(amount);
    this.stackHandler.grow(amount);
    this.cosmeticStackHandler.grow(amount);

    for (int i = 0; i < amount; i++) {
      this.renderHandler.add(true);
    }
    this.sizeShift += amount;
  }

  public void shrink(int amount) {
    this.validateSizeChange(amount);
    amount = Math.min(this.stackHandler.getSlots() - 1, amount);
    this.stackHandler.shrink(amount);
    this.cosmeticStackHandler.shrink(amount);

    for (int i = 0; i < amount; i++) {
      this.renderHandler.remove(this.renderHandler.size() - 1);
    }
    this.sizeShift -= amount;
  }

  private void validateSizeChange(int amount) {

    if (amount < 0) {
      throw new IllegalArgumentException("Amount cannot be negative!");
    }
  }

  public CompoundNBT serializeNBT() {
    CompoundNBT compoundNBT = new CompoundNBT();
    compoundNBT.put("Stacks", this.stackHandler.serializeNBT());
    compoundNBT.put("Cosmetics", this.cosmeticStackHandler.serializeNBT());

    ListNBT nbtTagList = new ListNBT();

    for (int i = 0; i < this.renderHandler.size(); i++) {
      CompoundNBT tag = new CompoundNBT();
      tag.putInt("Slot", i);
      tag.putBoolean("Render", this.renderHandler.get(i));
      nbtTagList.add(tag);
    }
    CompoundNBT nbt = new CompoundNBT();
    nbt.put("Renders", nbtTagList);
    nbt.putInt("Size", this.renderHandler.size());
    compoundNBT.put("Renders", nbt);
    compoundNBT.putInt("SizeShift", this.sizeShift);
    return compoundNBT;
  }

  public void deserializeNBT(CompoundNBT nbt) {

    if (nbt.contains("Stacks")) {
      this.stackHandler.deserializeNBT(nbt.getCompound("Stacks"));
    }

    if (nbt.contains("Cosmetics")) {
      this.cosmeticStackHandler.deserializeNBT(nbt.getCompound("Cosmetics"));
    }

    if (nbt.contains("Renders")) {
      CompoundNBT tag = nbt.getCompound("Renders");
      this.renderHandler = NonNullList.withSize(
          tag.contains("Size", Constants.NBT.TAG_INT) ? nbt.getInt("Size")
              : this.stackHandler.getSlots(), true);
      ListNBT tagList = tag.getList("Renders", Constants.NBT.TAG_COMPOUND);

      for (int i = 0; i < tagList.size(); i++) {
        CompoundNBT tags = tagList.getCompound(i);
        int slot = tags.getInt("Slot");

        if (slot >= 0 && slot < this.renderHandler.size()) {
          this.renderHandler.set(slot, tags.getBoolean("Render"));
        }
      }
    }

    if (nbt.contains("SizeShift")) {
      this.sizeShift = nbt.getInt("SizeShift");
    }
  }
}
