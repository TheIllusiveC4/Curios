package top.theillusivec4.curios.api.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public final class CurioStackHandler extends ItemStackHandler {

    protected NonNullList<ItemStack> previousStacks;

    public CurioStackHandler()
    {
        this(1);
    }

    public CurioStackHandler(int size) {
        this.setSize(size);
    }

    public CurioStackHandler(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
        this.previousStacks = NonNullList.create();

        for (ItemStack stack : stacks) {
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
}
