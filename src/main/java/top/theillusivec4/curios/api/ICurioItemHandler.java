package top.theillusivec4.curios.api;

import com.google.common.collect.ImmutableSortedMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.SortedMap;

public interface ICurioItemHandler {

    /**
     * An immutable map of the current curios, sorted by the {@link CurioType} identifier
     * @return  The current curios equipped
     */
    ImmutableSortedMap<String, ItemStackHandler> getCurioMap();

    /**
     * An immutable map of the previous curios, sorted by the {@link CurioType} identifier
     * This is only used to compare against {@link ICurioItemHandler#getCurioMap()} for content changes
     * Note that any changes made to the {@link ItemStackHandler} objects, but not its contents, in
     * {@link ICurioItemHandler#getCurioMap()} should be reflected in this map as well, otherwise syncing issues may occur
     * @return  The previous curios equipped
     */
    ImmutableSortedMap<String, ItemStackHandler> getPreviousCurioMap();

    /**
     * Sets the current curios map to the one passed in
     * @param map   The curios collection that will replace the current one
     */
    void setCurioMap(SortedMap<String, ItemStackHandler> map);

    /**
     * The number of slots across all {@link CurioType} identifiers
     * @return Number of slots
     */
    int getSlots();

    /**
     * The {@link ItemStackHandler} associated with the {@link CurioType} identifier
     *
     * @param identifier
     * @return
     */
    ItemStackHandler getStackHandler(String identifier);

    ItemStack getStackInSlot(String identifier, int slot);

    void setStackInSlot(String identifier, int slot, ItemStack stack);

    void enableCurio(String identifier);

    void disableCurio(String identifier);

    void addCurioSlot(String identifier, int amount);

    EntityLivingBase getWearer();
}
