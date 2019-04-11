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

package top.theillusivec4.curios.api.imc;

public class CurioIMCMessage {

    private final String identifier;
    private int size = 1;
    private boolean isEnabled = true;
    private boolean isHidden = false;

    public CurioIMCMessage(String id) {
        this.identifier = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getSize() {
        return size;
    }

    public CurioIMCMessage setSize(int size) {
        this.size = size;
        return this;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public CurioIMCMessage setEnabled(boolean enabled) {
        isEnabled = enabled;
        return this;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public CurioIMCMessage setHidden(boolean hidden) {
        isHidden = hidden;
        return this;
    }
}
