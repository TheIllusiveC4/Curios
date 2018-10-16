package c4.curios.api.inventory;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class CurioSlot {

    private final CurioSlotInfo info;

    private ItemStack stack;

    public CurioSlot(CurioSlotInfo info) {
        this.info = info;
        this.stack = ItemStack.EMPTY;
    }

    public CurioSlot(CurioSlotInfo info, ItemStack stack) {
        this.info = info;
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public CurioSlotInfo getInfo() {
        return info;
    }
}
