package top.theillusivec4.curios.api.capability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.CurioType;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;

import java.util.SortedMap;

public interface ICurioItemHandler {

    /**
     * An immutable map of the current curios, sorted by the {@link CurioType} identifier
     * @return  The current curios equipped
     */
    SortedMap<String, CurioStackHandler> getCurioMap();

    /**
     * Sets the current curios map to the one passed in
     * @param map   The curios collection that will replace the current one
     */
    void setCurioMap(SortedMap<String, CurioStackHandler> map);

    /**
     * The number of slots across all {@link CurioType} identifiers
     * @return Number of slots
     */
    int getSlots();

    CurioStackHandler getStackHandler(String identifier);

    ItemStack getStackInSlot(String identifier, int slot);

    void setStackInSlot(String identifier, int slot, ItemStack stack);

    void enableCurio(String identifier);

    void disableCurio(String identifier);

    void addCurioSlot(String identifier, int amount);

    void removeCurioSlot(String identifier, int amount);

    EntityLivingBase getWearer();

    SortedMap<String, CurioStackHandler> getDefaultSlots();
}
