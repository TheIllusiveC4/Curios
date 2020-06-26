package top.theillusivec4.curios.common.inventory;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
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
    NonNullList<ItemStack> newList = NonNullList.withSize(size, ItemStack.EMPTY);

    for (int i = 0; i < newList.size() && i < stacks.size(); i++) {
      newList.set(i, stacks.get(i));
    }
    return newList;
  }
}
