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

package top.theillusivec4.curios.api;

import java.util.Objects;
import javax.annotation.Nullable;

public final class CurioSlotType implements Comparable<CurioSlotType> {

  private final String identifier;
  private final int priority;
  /**
   * The default number of slots.
   */
  private int size;
  /**
   * Enabled slots will be given to holders by default.
   */
  private boolean locked;
  /**
   * Visible slots will shown in the default Curios GUI. Slots always exist, regardless of
   * visibility.
   */
  private boolean visible;

  public CurioSlotType(String identifier, @Nullable Integer priority) {
    this.identifier = identifier;
    this.priority = priority != null ? priority : 100;
    this.size = 1;
    this.locked = false;
    this.visible = true;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public int getPriority() {
    return this.priority;
  }

  public int getSize() {
    return this.size;
  }

  public void setSize(int size, boolean force) {
    this.size = force ? size : Math.max(size, this.size);
  }

  public boolean isLocked() {
    return this.locked;
  }

  public void setLocked(boolean locked, boolean force) {
    this.locked = force ? locked : this.locked || locked;
  }

  public boolean isVisible() {
    return this.visible;
  }

  public void setVisible(boolean visible, boolean force) {
    this.visible = force ? visible : this.visible && visible;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CurioSlotType that = (CurioSlotType) o;
    return identifier.equals(that.identifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifier);
  }

  @Override
  public int compareTo(CurioSlotType otherType) {

    if (this.priority == otherType.priority) {
      return this.identifier.compareTo(otherType.identifier);
    } else if (this.priority > otherType.priority) {
      return 1;
    } else {
      return -1;
    }
  }
}
