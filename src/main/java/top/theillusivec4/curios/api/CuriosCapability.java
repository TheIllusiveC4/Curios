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

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.items.IItemHandler;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public class CuriosCapability {

  public static final ResourceLocation ID_INVENTORY = CuriosResources.resource("inventory");
  public static final ResourceLocation ID_ITEM_HANDLER = CuriosResources.resource("item_handler");
  public static final ResourceLocation ID_ITEM = CuriosResources.resource("item");

  public static final EntityCapability<ICuriosItemHandler, Void> INVENTORY =
      EntityCapability.createVoid(ID_INVENTORY, ICuriosItemHandler.class);

  /**
   * An {@link IItemHandler} capability that can be accessed by external mods without requiring
   * a dependency on Curios or this class.
   */
  public static final EntityCapability<IItemHandler, Void> ITEM_HANDLER =
      EntityCapability.createVoid(ID_ITEM_HANDLER, IItemHandler.class);

  public static final ItemCapability<ICurio, Void> ITEM =
      ItemCapability.createVoid(ID_ITEM, ICurio.class);
}
