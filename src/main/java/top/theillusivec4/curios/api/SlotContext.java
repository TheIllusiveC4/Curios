/*
 * Copyright (c) 2018-2024 C4
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
 *
 */

package top.theillusivec4.curios.api;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jspecify.annotations.Nullable;
import top.theillusivec4.curios.api.common.inventory.CuriosResourceHandler;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/**
 * Record representing the accessible slot information related to its context.
 *
 * @param identifier The identifier of the slot type
 * @param entity     The wearer or intended wearer of the slot type
 * @param index      The index of the slot
 * @param cosmetic   True if the slot is cosmetic, false if the slot is functional
 * @param visible    True if the slot can render its item on the wearer, false if not
 */
public record SlotContext(String identifier, LivingEntity entity, int index, boolean cosmetic,
                          boolean visible) {

  @Nullable
  public ItemAccess getItemAccess() {
    ICuriosItemHandler inv = CuriosApi.getCuriosInventoryOrNull(entity);

    if (inv != null) {
      return inv.getStacksHandler(identifier).map(stacksHandler -> {
        IDynamicStackHandler stacks =
            cosmetic ? stacksHandler.getCosmeticStacks() : stacksHandler.getStacks();
        return ItemAccess.forHandlerIndex(new CuriosResourceHandler(stacks), index);
      }).orElse(null);
    }
    return null;
  }
}
