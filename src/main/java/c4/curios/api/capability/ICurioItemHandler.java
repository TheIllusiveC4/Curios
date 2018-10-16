package c4.curios.api.capability;

import c4.curios.api.inventory.CurioSlot;
import c4.curios.api.inventory.CurioSlotInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Tuple;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface ICurioItemHandler extends IItemHandlerModifiable {

    NonNullList<CurioSlot> getCurioStacks();

    void setCurioStacks(NonNullList<CurioSlot> curioStacks);
}
