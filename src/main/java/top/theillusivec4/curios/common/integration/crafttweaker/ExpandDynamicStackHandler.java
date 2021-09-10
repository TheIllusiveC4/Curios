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
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.data.MapData;
import com.blamejared.crafttweaker.impl.item.MCItemStack;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import org.openzen.zencode.java.ZenCodeType;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@ZenRegister
@Document("mods/Curios/IDynamicStackHandler")
@NativeTypeRegistration(value = IDynamicStackHandler.class, zenCodeName = "mods.curios.IDynamicStackHandler")
public class ExpandDynamicStackHandler {

    /**
     * Sets an {@link IItemStack} to the given slot index as the current stack.
     *
     * @param slot  The slot index
     * @param stack The {@link IItemStack} to assign as the current stack
     */
    @ZenCodeType.Method
    public static void setStackInSlot(IDynamicStackHandler internal, int slot, IItemStack stack) {
        internal.setStackInSlot(slot, stack.getInternal());
    }

    /**
     * Gets an {@link IItemStack} assigned as the current stack in the given slot index
     *
     * @param slot The slot index
     * @return The {@link IItemStack} assigned as the current stack
     */
    @ZenCodeType.Method
    public static IItemStack getStackInSlot(IDynamicStackHandler internal, int slot) {
        return new MCItemStack(internal.getStackInSlot(slot));
    }

    /**
     * Gets the {@link IItemStack} assigned as the previous stack in the given slot index
     *
     * @param slot The slot index
     * @return The {@link IItemStack} assigned as the previous stack
     */
    @ZenCodeType.Method
    public static IItemStack getPreviousStackInSlot(IDynamicStackHandler internal, int slot) {
        return new MCItemStack(internal.getPreviousStackInSlot(slot));
    }

    /**
     * Sets an {@link IItemStack} to the given slot index as the previous stack, for comparison purposes
     * with the current stack.
     *
     * @param slot  The slot index
     * @param stack The {@link IItemStack} to assign as the previous stack
     */
    @ZenCodeType.Method
    public static void setPreviousStackInSlot(IDynamicStackHandler internal, int slot, IItemStack stack) {
        internal.setPreviousStackInSlot(slot, stack.getInternal());
    }

    /**
     * @return The total number of slots
     */
    @ZenCodeType.Getter("slots")
    @ZenCodeType.Method
    public static int getSlots(IDynamicStackHandler internal) {
        return internal.getSlots();
    }

    /**
     * Increases the number of slots by the given amount.
     *
     * @param amount The number of slots to add
     */
    @ZenCodeType.Method
    public static void grow(IDynamicStackHandler internal, int amount) {
        internal.grow(amount);
    }

    /**
     * Decreases the number of slots by the given amount.
     *
     * @param amount The number of slots to remove
     */
    @ZenCodeType.Method
    public static void shrink(IDynamicStackHandler internal, int amount) {
        internal.shrink(amount);
    }

    @ZenCodeType.Caster(implicit = true)
    public static MapData toData(IDynamicStackHandler internal) {
        return new MapData(internal.serializeNBT());
    }
}
