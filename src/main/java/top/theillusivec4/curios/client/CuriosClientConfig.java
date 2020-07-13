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

package top.theillusivec4.curios.client;

public class CuriosClientConfig {

  public enum ButtonCorner {
    TOP_LEFT(26, -75, 73, -62), TOP_RIGHT(61, -75, 95, -62), BOTTOM_LEFT(26, -20, 73,
        -29), BOTTOM_RIGHT(61, -20, 95, -29);

    final int xoffset;
    final int yoffset;
    final int creativeXoffset;
    final int creativeYoffset;

    ButtonCorner(int x, int y, int creativeX, int creativeY) {
      xoffset = x;
      yoffset = y;
      creativeXoffset = creativeX;
      creativeYoffset = creativeY;
    }

    public int getXoffset() {
      return xoffset;
    }

    public int getYoffset() {
      return yoffset;
    }

    public int getCreativeXoffset() {
      return creativeXoffset;
    }

    public int getCreativeYoffset() {
      return creativeYoffset;
    }
  }
}
