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
import com.blamejared.crafttweaker.impl.data.MapData;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import org.openzen.zencode.java.ZenCodeType;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@ZenRegister
@Document("mods/Curios/ICuriosStacksHandler")
@NativeTypeRegistration(value = ICurioStacksHandler.class, zenCodeName = "mods.curios.ICuriosStacksHandler")
public class ExpandCuriosStacksHandler {
    /**
     * Gets the {@link IDynamicStackHandler} for the equipped curio stacks.
     *
     * @return The {@link IDynamicStackHandler} for the equipped curio stacks
     */
    @ZenCodeType.Getter("stacks")
    @ZenCodeType.Method
    public static IDynamicStackHandler getStacks(ICurioStacksHandler internal) {
        return internal.getStacks();
    }

    /**
     * Gets a list of boolean values that represent render states. True for rendering and false for no
     * rendering.
     * <br>
     * The size of this list should always match the size of {@link ICurioStacksHandler#getStacks()}.
     *
     * @return A list of boolean values for render states
     */
    @ZenCodeType.Getter("cosmeticStacks")
    @ZenCodeType.Method
    public static IDynamicStackHandler getCosmeticStacks(ICurioStacksHandler internal) {
        return internal.getCosmeticStacks();
    }

    /**
     * Gets the number of slots for equipped curio stacks.
     * <br>
     * This number should always match the size of {@link ICurioStacksHandler#getStacks()}
     *
     * @return The number of slots for equipped curio stacks.
     */
    @ZenCodeType.Getter("slots")
    @ZenCodeType.Method
    public static int getSlots(ICurioStacksHandler internal) {
        return internal.getSlots();
    }

    /**
     * Gets the size offset for this instance. This value is used to persist size changes for this
     * handler even when the underlying size changes.
     *
     * @return The number of the size offset for this instance
     */
    @ZenCodeType.Getter("sizeShift")
    @ZenCodeType.Method
    public static int getSizeShift(ICurioStacksHandler internal) {
        return internal.getSizeShift();
    }

    /**
     * Gets whether or not this stack handler should be visible. This does not lock the stack handler
     * from being used regardless.
     *
     * @return True or false for visibility
     */
    @ZenCodeType.Getter("visible")
    @ZenCodeType.Method
    public static boolean isVisible(ICurioStacksHandler internal) {
        return internal.isVisible();
    }

    /**
     * Gets whether or not this stack handler has cosmetic handling. This does not lock the cosmetic
     * stack handler from being used regardless.
     *
     * @return True or false for cosmetic handling
     */
    @ZenCodeType.Getter("hasCosmetic")
    @ZenCodeType.Method
    public static boolean hasCosmetic(ICurioStacksHandler internal) {
        return internal.hasCosmetic();
    }

    /**
     * Increases the number of slots by the given amount.
     *
     * @param amount The number of slots to add to the handler
     */
    @ZenCodeType.Method
    public static void grow(ICurioStacksHandler internal, int amount) {
        internal.grow(amount);
    }

    /**
     * Decreases the number of slots by the given amount. This should not decrease the final number of
     * slots below 1.
     *
     * @param amount The number of slots to remove from the handler
     */
    @ZenCodeType.Method
    public static void shrink(ICurioStacksHandler internal, int amount) {
        internal.shrink(amount);
    }

    @ZenCodeType.Caster(implicit = true)
    public static MapData toData(ICurioStacksHandler internal) {
        return new MapData(internal.serializeNBT());
    }
}
