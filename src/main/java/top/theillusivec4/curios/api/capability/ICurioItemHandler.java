package top.theillusivec4.curios.api.capability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.CurioType;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;

import java.util.SortedMap;

public interface ICurioItemHandler {

    /**
     * A unmodifiable view of the map of the current curios, sorted by the {@link CurioType} identifier
     * @return  The current curios equipped
     */
    SortedMap<String, CurioStackHandler> getCurioMap();

    /**
     * Sets the current curios map to the one passed in
     * @param map   The curios collection that will replace the current one
     */
    void setCurioMap(SortedMap<String, CurioStackHandler> map);

    /**
     * @return The number of slots across all {@link CurioType} identifiers
     */
    int getSlots();

    /**
     * @param identifier The identifier for the {@link CurioType}
     * @return The {@link CurioStackHandler} associated with the given {@link CurioType} identifier
     */
    CurioStackHandler getStackHandler(String identifier);

    /**
     * @param identifier    The identifier for the {@link CurioType}
     * @param slot          The slot index of the {@link CurioStackHandler} for the given identifier
     * @return The ItemStack in the slot
     */
    ItemStack getStackInSlot(String identifier, int slot);

    /**
     * Sets the ItemStack in the given slot index for the given {@link CurioType} identifier
     * @param identifier    The identifier for the {@link CurioType}
     * @param slot          The slot index of the {@link CurioStackHandler} for the given identifier
     * @param stack         The ItemStack to place in the slot
     */
    void setStackInSlot(String identifier, int slot, ItemStack stack);

    /**
     * Enables the {@link CurioType} for a given identifier, adding the default settings to the curio map
     * @param identifier    The identifier for the {@link CurioType}
     */
    void enableCurio(String identifier);

    /**
     * Disables the {@link CurioType} for a given identifier, removing it from the curio map
     * Note that the default implementation handles catching and returning ItemStacks that are found in these slots
     * @param identifier    The identifier for the {@link CurioType}
     */
    void disableCurio(String identifier);

    /**
     * Adds an amount of slots to the {@link CurioStackHandler} of a {@link CurioType} associated with the identifier
     * @param identifier    The identifier for the {@link CurioType}
     * @param amount        The number of slots to add, must be non-negative
     */
    void addCurioSlot(String identifier, int amount);

    /**
     * Removes an amount of slots from the {@link CurioStackHandler} of a {@link CurioType} associated with the identifier
     * Note that the default implementation handles catching and returning ItemStacks that are found in these slots
     * @param identifier    The identifier for the {@link CurioType}
     * @param amount        The number of slots to remove, must be non-negative
     */
    void removeCurioSlot(String identifier, int amount);

    /**
     * @return  The wearer/owner of this handler instance
     */
    EntityLivingBase getWearer();

    /**
     * The default curio map built from the settings found in {@link top.theillusivec4.curios.api.CuriosRegistry},
     * sorted by {@link CurioType} identifier
     * Used primarily for initializing and resetting the current curio map
     * @return  A default curio map from the registry
     */
    SortedMap<String, CurioStackHandler> getDefaultSlots();

    /**
     * Adds an ItemStack to the invalid cache
     * An internal helper method for storing items found in the process of disabling/removing slots to be given back to
     * the player or dropped on the ground in other cases
     * @param stack The ItemStack to add
     */
    void addInvalid(ItemStack stack);

    /**
     * Drops all of the ItemStacks found in the invalid cache
     * An internal helper method for dropping items found in disabling/removing slots or giving them back to players
     */
    void dropInvalidCache();
}
