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

package top.theillusivec4.curios.api.type;

import net.minecraft.resources.ResourceLocation;

public interface ISlotType extends Comparable<ISlotType> {

  /**
   * @return The identifier for this slot type
   */
  String getIdentifier();

  /**
   * @return The {@link ResourceLocation} for the icon associated with this slot type
   */
  ResourceLocation getIcon();

  /**
   * @return The priority of this slot type for ordering
   */
  int getPriority();

  /**
   * @return The number of slots to give by default for this slot type
   */
  int getSize();

  /**
   * @deprecated Check if {@link ISlotType#getSize()} returns 0
   */
  @Deprecated
  default boolean isLocked() {
    return getSize() == 0;
  }

  /**
   * @return True if the slot type should be visible, false otherwise
   */
  boolean isVisible();

  /**
   * @return True if the slot type has active cosmetic slots, false otherwise
   */
  boolean hasCosmetic();
}
