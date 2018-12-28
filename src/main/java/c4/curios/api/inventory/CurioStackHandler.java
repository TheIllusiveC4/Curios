package c4.curios.api.inventory;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class CurioStackHandler implements IItemHandlerModifiable {

    private final CurioSlotEntry entry;
    private NonNullList<ItemStack> stacks;

    public CurioStackHandler(@Nonnull CurioSlotEntry entry) {
        this.entry = entry;
        this.stacks = NonNullList.withSize(entry.getSize(), ItemStack.EMPTY);
    }

    public CurioStackHandler(@Nonnull CurioSlotEntry entry, NonNullList<ItemStack> stacks) {
        this.entry = entry;
        this.stacks = stacks;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return stacks.get(slot);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        stacks.set(slot, stack);
    }

    public NonNullList<ItemStack> getStacks() {
        return this.stacks;
    }

    public void setSize(int size, @Nonnull EntityLivingBase livingBase) {

        if (size != stacks.size()) {
            NonNullList<ItemStack> activeStacks = NonNullList.withSize(size, ItemStack.EMPTY);

            for (int i = 0; i < size; i++) {
                activeStacks.set(i, stacks.get(i));
            }

            if (size < stacks.size()) {

                if (livingBase instanceof EntityPlayer) {

                    for (int i = size; i < stacks.size(); i++) {
                        ItemHandlerHelper.giveItemToPlayer((EntityPlayer) livingBase, stacks.get(i));
                    }
                } else {

                    for (int i = size; i < stacks.size(); i++) {
                        livingBase.entityDropItem(stacks.get(i), 0.0f);
                    }
                }
            }
            this.stacks = activeStacks;
        }
    }

    @Nonnull
    public CurioSlotEntry getEntry() {
        return entry;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

        if (slot < this.getSlots() && !stack.isEmpty() && this.isItemValid(slot, stack)) {

            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            this.validateSlotIndex(slot);
            ItemStack existing = this.stacks.get(slot);
            int limit = stack.getMaxStackSize();

            if (!existing.isEmpty()) {
                if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) return stack;
                limit -= existing.getCount();
            }

            if (limit <= 0) {
                return stack;
            }
            boolean reachedLimit = stack.getCount() > limit;

            if (!simulate) {

                if (existing.isEmpty()) {
                    this.stacks.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
                } else {
                    existing.grow(reachedLimit ? limit : stack.getCount());
                }
            }
            return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount()- limit) : ItemStack.EMPTY;
        }
        return stack;
    }

    private void validateSlotIndex(int slot) {

        if (slot < 0 || slot >= stacks.size()) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
        }
    }


    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {

        if (amount == 0) {
            return ItemStack.EMPTY;
        }
        this.validateSlotIndex(slot);
        ItemStack existing = this.stacks.get(slot);

        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {

            if (!simulate) {
                this.stacks.set(slot, ItemStack.EMPTY);
            }
            return existing;
        } else {

            if (!simulate) {
                this.stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
            }
            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }
}
