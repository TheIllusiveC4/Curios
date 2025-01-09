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

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

/**
 * A wrapper class for representing slot types as attributes for use in attribute modifiers
 */
public class SlotAttribute extends Attribute {

  private static final Map<String, Holder<? extends Attribute>> SLOT_ATTRIBUTES = new HashMap<>();

  private final String identifier;

  @SuppressWarnings("unchecked")
  public static Holder<Attribute> getOrCreate(String id) {
    return (Holder<Attribute>) SLOT_ATTRIBUTES.computeIfAbsent(id,
        (k) -> new Holder.Direct<>(new SlotAttribute(id)));
  }

  protected SlotAttribute(String identifier) {
    super("curios.identifier." + identifier, 0);
    this.identifier = identifier;
  }

  public String getIdentifier() {
    return this.identifier;
  }
}
