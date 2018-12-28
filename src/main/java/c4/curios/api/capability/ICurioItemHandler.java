package c4.curios.api.capability;

import c4.curios.api.inventory.CurioStackHandler;
import net.minecraft.item.ItemStack;

import java.util.Map;

public interface ICurioItemHandler {

    Map<String, CurioStackHandler> getCurioMap();

    void setCurioMap(Map<String, CurioStackHandler> map);

    int getSlots();

    CurioStackHandler getStackHandler(String identifier);

    ItemStack getStackInSlot(String identifier, int slot);

    void setStackInSlot(String identifier, int slot, ItemStack stack);
}
