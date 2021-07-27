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

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.curios.api.SlotTypeMessage.Builder;

/**
 * These slot type presets should be used whenever applicable by calling {@link
 * SlotTypePreset#getMessageBuilder()} and building off that in the IMC message
 */
public enum SlotTypePreset {
  HEAD("head", 40), NECKLACE("necklace", 60), BACK("back", 80), BODY("body", 100), BRACELET(
      "bracelet", 120), HANDS("hands", 140), RING("ring", 160), BELT("belt", 180), CHARM("charm",
      200), CURIO("curio", 20);

  final String id;
  final int priority;

  SlotTypePreset(String id, int priority) {
    this.id = id;
    this.priority = priority;
  }

  /**
   * Attempts to find a valid preset for the given identifier and returns {@link Optional#empty()}
   * if none is found.
   *
   * @param id The {@link top.theillusivec4.curios.api.type.ISlotType} identifier
   * @return {@link Optional} wrapper for the found preset or {@link Optional#empty()}
   */
  public static Optional<SlotTypePreset> findPreset(String id) {
    try {
      return Optional.of(SlotTypePreset.valueOf(id.toUpperCase()));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public String getIdentifier() {
    return this.id;
  }

  public Builder getMessageBuilder() {
    return new Builder(this.id).priority(this.priority).icon(
        new ResourceLocation(CuriosApi.MODID, "item/empty_" + this.getIdentifier() + "_slot"));
  }
}
