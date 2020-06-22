package top.theillusivec4.curios.api.inventory;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

public class DynamicStackHandler extends ItemStackHandler {

  protected NonNullList<ItemStack> previousStacks;

  public DynamicStackHandler(int size) {
    super(size);
    this.previousStacks = NonNullList.withSize(size, ItemStack.EMPTY);
  }

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
  public ItemStack getPreviousStackInSlot(int slot) {
    this.validateSlotIndex(slot);
    return this.previousStacks.get(slot);
  }

  public void grow(int amount) {

    for (int i = 0; i < amount; i++) {
      this.stacks.add(ItemStack.EMPTY);
    }
  }

  public void shrink(int amount) {
    int targetSize = this.stacks.size() - amount;

    while (this.stacks.size() > targetSize) {
      this.stacks.remove(this.stacks.size() - 1);
    }
  }
}
