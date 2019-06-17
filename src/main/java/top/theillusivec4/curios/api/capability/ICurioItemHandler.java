/*
 * Copyright (C) 2018-2019  C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.curios.api.capability;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.CurioType;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;

import java.util.Set;
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
    LivingEntity getWearer();

    /**
     * The default curio map built from the settings found in {@link top.theillusivec4.curios.api.CuriosRegistry},
     * sorted by {@link CurioType} identifier
     * Used primarily for initializing and resetting the current curio map
     * @return  A default curio map from the registry
     */
    SortedMap<String, CurioStackHandler> getDefaultSlots();

    /**
     * Used internally for retrieving a list of disabled identifiers
     * @return A list of disabled {@link CurioType} by identifier for this handler
     */
    ImmutableSet<String> getDisabled();

    /**
     * Used internally for setting the list of disabled {@link CurioType} by identifier for this handler
     * @param disabled List of disabled identifiers
     */
    void setDisabled(Set<String> disabled);

    /**
     * Adds an ItemStack to the invalid cache
     * Used internally for storing items found in the process of disabling/removing slots to be given back to
     * the player or dropped on the ground in other cases
     * @param stack The ItemStack to add
     */
    void addInvalid(ItemStack stack);

    /**
     * Drops all of the ItemStacks found in the invalid cache
     * Used internally for dropping items found in disabling/removing slots or giving them back to players
     */
    void dropInvalidCache();
}
