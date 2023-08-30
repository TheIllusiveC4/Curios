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

package top.theillusivec4.curios.api.type.util;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * @deprecated Moved to internal code
 */
@Deprecated(forRemoval = true, since = "1.20.1")
@ApiStatus.ScheduledForRemoval(inVersion = "1.22")
public interface IIconHelper {

  /**
   * @deprecated Moved to internal code and removed from the API
   */
  @Deprecated(forRemoval = true, since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  void clearIcons();

  /**
   * @deprecated Moved to internal code and removed from the API
   */
  @Deprecated(forRemoval = true, since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  void addIcon(String identifier, ResourceLocation resourceLocation);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#getSlotIcon(String)}
   */
  @Deprecated(forRemoval = true, since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  ResourceLocation getIcon(String identifier);
}
