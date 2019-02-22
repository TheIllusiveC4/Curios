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

package top.theillusivec4.curios.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IInteractionObject;
import top.theillusivec4.curios.Curios;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CurioContainerHandler implements IInteractionObject {

    public static final ResourceLocation ID = new ResourceLocation(Curios.MODID, "container");

    @Nonnull
    @Override
    public Container createContainer(@Nonnull InventoryPlayer inventoryPlayer, @Nonnull EntityPlayer entityPlayer) {
        return new ContainerCurios(inventoryPlayer, entityPlayer);
    }

    @Nonnull
    @Override
    public String getGuiID() {
        return ID.toString();
    }

    @Nonnull
    @Override
    public ITextComponent getName() {
        return new TextComponentString(getGuiID());
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return null;
    }
}
