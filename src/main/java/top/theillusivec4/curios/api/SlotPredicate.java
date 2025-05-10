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

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.advancements.critereon.MinMaxBounds;

/**
 * Predicate for matching a filter of slots and indices to a particular {@link SlotContext}.
 *
 * @param slots The list of slots to match against
 * @param index The list of indices in matching slots to match against
 */
public record SlotPredicate(List<String> slots, MinMaxBounds.Ints index) {

  public static final Codec<SlotPredicate> CODEC = RecordCodecBuilder.create(
      slotPredicateInstance -> slotPredicateInstance.group(
              Codec.STRING.listOf().optionalFieldOf("slots", List.of())
                  .forGetter(SlotPredicate::slots),
              MinMaxBounds.Ints.CODEC.optionalFieldOf("index", MinMaxBounds.Ints.ANY)
                  .forGetter(SlotPredicate::index)
          )
          .apply(slotPredicateInstance, SlotPredicate::new)
  );

  public boolean matches(SlotContext slotContext) {

    if (!this.slots.contains(slotContext.identifier())) {
      return false;
    } else {
      return this.index.matches(slotContext.index());
    }
  }

  public static class Builder {

    private Set<String> identifiers = new HashSet<>();
    private MinMaxBounds.Ints indices = MinMaxBounds.Ints.ANY;

    private Builder() {
    }

    public static Builder slot() {
      return new Builder();
    }

    public Builder of(String... identifiers) {
      this.identifiers = Stream.of(identifiers).collect(ImmutableSet.toImmutableSet());
      return this;
    }

    public Builder withIndex(MinMaxBounds.Ints index) {
      this.indices = index;
      return this;
    }

    public SlotPredicate build() {
      return new SlotPredicate(this.identifiers.stream().toList(), this.indices);
    }
  }
}
