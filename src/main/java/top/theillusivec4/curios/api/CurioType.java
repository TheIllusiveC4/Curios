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

public final class CurioType {

    private final String identifier;
    /** The default number of slots*/
    private int size;
    /** Enabled slots will be given to holders by default*/
    private boolean isEnabled;
    /** Hidden slots will not show up in the default Curios GUI, but will still exist*/
    private boolean isHidden;

    public CurioType(String identifier) {
        this.identifier = identifier;
        this.size = 1;
        this.isEnabled = true;
        this.isHidden = false;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getSize() {
        return this.size;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isHidden() { return isHidden; }

    public final CurioType defaultSize(int size) {
        this.size = Math.max(size, this.size);
        return this;
    }

    public final CurioType enabled(boolean enabled) {
        this.isEnabled = enabled;
        return this;
    }

    public final CurioType hide(boolean hide) {
        this.isHidden = hide;
        return this;
    }
}
