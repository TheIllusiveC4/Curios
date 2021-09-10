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
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.tag.manager.TagManagerItem;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.type.ISlotType;

@ZenRegister
@Document("mods/Curios/ICuriosSlotType")
@NativeTypeRegistration(value = ISlotType.class, zenCodeName = "mods.curios.ICuriosSlotType")
public class ExpandSlotType {
    /**
     * @return The identifier for this slot type
     */
    @ZenCodeType.Getter("identifier")
    @ZenCodeType.Method
    public static String getIdentifier(ISlotType slotType) {
        return slotType.getIdentifier();
    }

    /**
     * @return The ResourceLocation for the icon associated with this slot type
     */
    @ZenCodeType.Getter("icon")
    @ZenCodeType.Method
    public static ResourceLocation getIcon(ISlotType slotType) {
        return slotType.getIcon();
    }

    /**
     * @return The priority of this slot type for ordering
     */
    @ZenCodeType.Getter("priority")
    @ZenCodeType.Method
    public static int getPriority(ISlotType slotType) {
        return slotType.getPriority();
    }

    /**
     * @return The number of slots to give by default for this slot type
     */
    @ZenCodeType.Getter("size")
    @ZenCodeType.Method
    public static int getSize(ISlotType slotType) {
        return slotType.getSize();
    }

    /**
     * @return True if the slot type should be locked by default and not usable until unlocked, false
     * otherwise
     */
    @ZenCodeType.Getter("locked")
    @ZenCodeType.Method
    public static boolean isLocked(ISlotType slotType) {
        return slotType.isLocked();
    }

    @ZenCodeType.Getter("visible")
    @ZenCodeType.Method
    public static boolean isVisible(ISlotType slotType) {
        return slotType.isVisible();
    }

    @ZenCodeType.Getter("hasCosmetic")
    @ZenCodeType.Method
    public static boolean hasCosmetic(ISlotType slotType) {
        return slotType.hasCosmetic();
    }

    @ZenCodeType.Getter("commandString")
    public static String getCommandString(ISlotType slotType) {
        return "<curiosslottype:" + slotType + ">";
    }

    @ZenCodeType.Method
    @ZenCodeType.Caster
    public static MCTag<Item> asTag(ISlotType slotType) {
        return TagManagerItem.INSTANCE.getTag(new ResourceLocation(Curios.MODID, slotType.getIdentifier()));
    }
}
