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

package top.theillusivec4.curios.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.capability.CuriosCapability;
import top.theillusivec4.curios.api.capability.ICurio;
import top.theillusivec4.curios.api.capability.ICurioItemHandler;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class CuriosAPI {

    public static LazyOptional<ICurio> getCurio(ItemStack stack) {
        return stack.getCapability(CuriosCapability.ITEM);
    }

    public static LazyOptional<ICurioItemHandler> getCuriosHandler(@Nonnull final EntityLivingBase entityLivingBase) {
        return entityLivingBase.getCapability(CuriosCapability.INVENTORY);
    }

    /**
     * Gets the first found ItemStack of the item type equipped in a curio slot, or null if no matches were found.
     * @param item              The item to find
     * @param entityLivingBase  The wearer of the item to be found
     * @return  A data pair indicating the identifier of the curio slot and the slot index of the first found ItemStack
     * matching the parameters. Null if no matches were found.
     */
    @Nullable
    public static Tuple<String, Integer> getCurioEquipped(Item item, @Nonnull final EntityLivingBase entityLivingBase) {
        Tuple<String, Integer> found = getCuriosHandler(entityLivingBase).map(handler -> {
            Set<String> tags = CuriosRegistry.getCurioTags(item);

            for (String id : tags) {
                CurioStackHandler stackHandler = handler.getStackHandler(id);

                if (stackHandler != null) {

                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack stack = stackHandler.getStackInSlot(i);

                        if (!stack.isEmpty() && item == stack.getItem()) {
                            return new Tuple<>(id, i);
                        }
                    }
                }
            }
            return new Tuple<>("", 0);
        }).orElse(new Tuple<>("", 0));

        if (!found.getA().isEmpty()) {
            return found;
        } else {
            return null;
        }
    }

    public static void setTypeEnabled(String id, boolean enabled) {
        CurioType type = CuriosRegistry.getType(id);

        if (type != null) {
            type.enabled(enabled);
        }
    }

    public static void setTypeSize(String id, int size) {
        CurioType type = CuriosRegistry.getType(id);

        if (type != null) {
            type.defaultSize(size);
        }
    }

    public static void setTypeHidden(String id, boolean hide) {
        CurioType type = CuriosRegistry.getType(id);

        if (type != null) {
            type.hide(hide);
        }
    }

    public static void addTypeSlotToEntity(String id, final EntityLivingBase entityLivingBase) {
        addTypeSlotsToEntity(id, 1, entityLivingBase);
    }

    public static void addTypeSlotsToEntity(String id, int amount, final EntityLivingBase entityLivingBase) {
        getCuriosHandler(entityLivingBase).ifPresent(handler -> handler.addCurioSlot(id, amount));
    }

    public static void removeTypeSlotFromEntity(String id, final EntityLivingBase entityLivingBase) {
        removeTypeSlotsFromEntity(id, 1, entityLivingBase);
    }

    public static void removeTypeSlotsFromEntity(String id, int amount, final EntityLivingBase entityLivingBase) {
        getCuriosHandler(entityLivingBase).ifPresent(handler -> handler.removeCurioSlot(id, amount));
    }

    public static void enableTypeForEntity(String id, final EntityLivingBase entityLivingBase) {
        getCuriosHandler(entityLivingBase).ifPresent(handler -> handler.enableCurio(id));
    }

    public static void disableTypeForEntity(String id, final EntityLivingBase entityLivingBase) {
        getCuriosHandler(entityLivingBase).ifPresent(handler -> handler.disableCurio(id));
    }
}
