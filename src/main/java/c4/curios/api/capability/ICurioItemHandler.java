package c4.curios.api.capability;

import com.google.common.collect.ImmutableMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Map;

public interface ICurioItemHandler {

    ImmutableMap<String, ItemStackHandler> getCurioMap();

    ImmutableMap<String, ItemStackHandler> getPreviousCurioMap();

    void setCurioMap(Map<String, ItemStackHandler> map);

    /**
    * Returns the total number of Curio slots across all identifiers
    */
    int getSlots();

    ItemStackHandler getStackHandler(String identifier);

    ItemStack getStackInSlot(String identifier, int slot);

    void setStackInSlot(String identifier, int slot, ItemStack stack);

    void addCurioSlot(String identifier);

    void removeCurioSlot(String identifier);
}
