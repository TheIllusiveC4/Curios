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

import javax.annotation.Nonnull;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;

public class CuriosTriggers {

  /**
   * Builds a new {@link EquipBuilder} for data generation using the trigger that fires when a
   * curio item is equipped
   *
   * @return A new builder instance
   */
  @Nonnull
  public static EquipBuilder equip() {
    return new EquipBuilder();
  }

  public static final class EquipBuilder {

    private ItemPredicate.Builder itemPredicate;
    private LocationPredicate.Builder locationPredicate;
    private SlotPredicate.Builder slotPredicate;

    private EquipBuilder() {
    }

    public EquipBuilder withItem(ItemPredicate.Builder builder) {
      this.itemPredicate = builder;
      return this;
    }

    public EquipBuilder withLocation(LocationPredicate.Builder builder) {
      this.locationPredicate = builder;
      return this;
    }

    public EquipBuilder withSlot(SlotPredicate.Builder builder) {
      this.slotPredicate = builder;
      return this;
    }

    public Criterion<? extends CriterionTriggerInstance> build() {
      return new Criterion<>(null, null);
    }
  }
}
