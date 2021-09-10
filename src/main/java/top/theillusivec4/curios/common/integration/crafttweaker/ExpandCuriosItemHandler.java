/*
 * Copyright (c) 2018-2020 C4
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

package top.theillusivec4.curios.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.openzen.zencode.java.ZenCodeType;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles all item stacks of curios. You can get it by calling `curiosItemHandler` getter of {@link PlayerEntity}
 *
 * @docParam this player.curiosItemHandler
 */
@ZenRegister
@Document("mods/Curios/ICuriosItemHandler")
@NativeTypeRegistration(value = ICuriosItemHandler.class, zenCodeName = "mods.curios.ICuriosItemHandler")
public class ExpandCuriosItemHandler {
    /**
     * A map of the current curios
     *
     * @return The current curios equipped
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("curios")
    public static Map<ISlotType, ICurioStacksHandler> getCurios(ICuriosItemHandler internal) {
        return internal.getCurios().entrySet().stream().collect(
                Collectors.toMap(
                        entry -> CuriosBracketHandlers.getSlotType(entry.getKey()),
                        Map.Entry::getValue
                )
        );
    }

    /**
     * Gets the number of slots across all {@link ISlotType} identifiers.
     *
     * @return The number of slots
     */
    @ZenCodeType.Getter("slots")
    @ZenCodeType.Method
    public static int getSlots(ICuriosItemHandler internal) {
        return internal.getSlots();
    }

    /**
     * Gets the number of visible slots across all {@link ISlotType} identifiers.
     *
     * @return The number of visible slots
     */
    @ZenCodeType.Getter("visibleSlots")
    @ZenCodeType.Method
    public static int getVisibleSlots(ICuriosItemHandler internal) {
        return internal.getVisibleSlots();
    }

    @ZenCodeType.Getter("lockedSlots")
    @ZenCodeType.Method
    public static List<ISlotType> getLockedSlots(ICuriosItemHandler internal) {
        return internal.getLockedSlots().stream().map(CuriosBracketHandlers::getSlotType).collect(Collectors.toList());
    }

    /**
     * Gets the an {@link ICurioStacksHandler} associated with the given {@link ISlotType}
     * Returns null if it it doesn't exist
     *
     * @return The stack handler
     * @docParam slotType <curiosslottype:belt>
     */
    @ZenCodeType.Method
    @ZenCodeType.Nullable
    public static ICurioStacksHandler getStackHandler(ICuriosItemHandler internal, ISlotType slotType) {
        return internal.getStacksHandler(slotType.getIdentifier()).orElse(null);
    }

    /**
     * Resets the current curios map to default values.
     */
    @ZenCodeType.Method
    public static void reset(ICuriosItemHandler internal) {
        internal.reset();
    }

    /**
     * Enables the {@link ISlotType}, adding the default settings to the curio map.
     *
     * @param slotType The {@link ISlotType} to unlock
     * @param amount   The amount of slots to unlock
     *
     * @docParam slotType <curiosslottype:belt>
     * @docParam amount 1
     * @docParam visible true
     * @docParam cosmetic true
     */
    @ZenCodeType.Method
    public static void unlockSlotType(ICuriosItemHandler internal, ISlotType slotType, int amount, boolean visible, boolean cosmetic) {
        internal.unlockSlotType(slotType.getIdentifier(), amount, visible, cosmetic);
    }

    /**
     * Disables the {@link ISlotType}, removing it from the curio map.
     *
     * @docParam slotType <curiosslottype:belt>
     */
    @ZenCodeType.Method
    public static void lockSlotType(ICuriosItemHandler internal, ISlotType slotType) {
        internal.lockSlotType(slotType.getIdentifier());
    }

    /**
     * Removes an amount of slots from the {@link ICurioStacksHandler} of an {@link ISlotType}
     *
     * @param amount The number of slots to remove, must be non-negative
     *
     * @docParam slotType <curiosslottype:belt>
     * @docParam amount 1
     */
    @ZenCodeType.Method
    public static void growSlotType(ICuriosItemHandler internal, ISlotType slotType, int amount) {
        internal.growSlotType(slotType.getIdentifier(), amount);
    }

    @ZenCodeType.Method
    @ZenCodeType.Getter("wearer")
    public static LivingEntity getWearer(ICuriosItemHandler internal) {
        return internal.getWearer();
    }

    /**
     * Returns the total Looting bonus of all fortune curios.
     * Recalculated with each LivingUpdateEvent.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("fortuneBonus")
    public static int getFortuneBonus(ICuriosItemHandler internal) {
        return internal.getFortuneBonus();
    }

    /**
     * Returns the total Looting bonus of all equipped curios.
     * Recalculated with each LivingUpdateEvent.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("lootingBonus")
    public static int getLootingBonus(ICuriosItemHandler internal) {
        return internal.getLootingBonus();
    }
}
