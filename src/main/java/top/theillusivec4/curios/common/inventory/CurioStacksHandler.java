package top.theillusivec4.curios.common.inventory;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CurioStacksHandler implements ICurioStacksHandler {

  private IDynamicStackHandler stackHandler;
  private IDynamicStackHandler cosmeticStackHandler;
  private int sizeShift;
  private boolean visible;
  private boolean cosmetic;
  private NonNullList<Boolean> renderHandler;

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
    this.renderHandler = NonNullList.withSize(size, true);
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
  public NonNullList<Boolean> getRenders() {
    return this.renderHandler;
  }

  @Override
  public int getSlots() {
    return this.stackHandler.getSlots();
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
    NonNullList<Boolean> newList = NonNullList.withSize(this.renderHandler.size() + amount, true);

    for (int i = 0; i < newList.size() && i < this.renderHandler.size(); i++) {
      newList.set(i, renderHandler.get(i));
    }
    this.renderHandler = newList;
    this.sizeShift += amount;
  }

  @Override
  public void shrink(int amount) {
    this.validateSizeChange(amount);
    amount = Math.min(this.stackHandler.getSlots() - 1, amount);
    this.stackHandler.shrink(amount);
    this.cosmeticStackHandler.shrink(amount);
    NonNullList<Boolean> newList = NonNullList.withSize(this.renderHandler.size() - amount, true);

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
    compoundNBT.putBoolean("HasCosmetic", this.cosmetic);
    compoundNBT.putBoolean("Visible", this.visible);
    return compoundNBT;
  }

  @Override
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
          nbt.contains("Size", Constants.NBT.TAG_INT) ? nbt.getInt("Size")
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
    this.cosmetic = nbt.contains("HasCosmetic") ? nbt.getBoolean("HasCosmetic") : this.cosmetic;
    this.visible = nbt.contains("Visible") ? nbt.getBoolean("Visible") : this.visible;
  }
}
