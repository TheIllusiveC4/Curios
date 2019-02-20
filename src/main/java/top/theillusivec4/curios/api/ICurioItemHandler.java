package top.theillusivec4.curios.api;

import com.google.common.collect.ImmutableSortedMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.SortedMap;

public interface ICurioItemHandler {

    ImmutableSortedMap<String, ItemStackHandler> getCurioMap();

    ImmutableSortedMap<String, ItemStackHandler> getPreviousCurioMap();

    void setCurioMap(SortedMap<String, ItemStackHandler> map);

    /**
    * Returns the total number of Curio slots across all identifiers
    */
    int getSlots();

    ItemStackHandler getStackHandler(String identifier);

    ItemStack getStackInSlot(String identifier, int slot);

    void setStackInSlot(String identifier, int slot, ItemStack stack);

    void enableCurio(String identifier);

    void disableCurio(String identifier);

    void addCurioSlot(String identifier, int amount);

    EntityLivingBase getWearer();
}
