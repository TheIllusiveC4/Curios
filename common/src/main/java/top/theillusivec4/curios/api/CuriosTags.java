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

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Collection of tags used by Curios. */
public final class CuriosTags {

  /** Items worn on the back, such as capes or backpacks. */
  public static final TagKey<Item> BACK = createItemTag("back");

  /** Items worn around the waist, such as belts or pouches. */
  public static final TagKey<Item> BELT = createItemTag("belt");

  /** Items worn on the torso/chest, such as cloaks or shirts. */
  public static final TagKey<Item> BODY = createItemTag("body");

  /** Items worn around the wrist, such as bands or bracelets. */
  public static final TagKey<Item> BRACELET = createItemTag("bracelet");

  /**
   * Miscellaneous items that are not strongly associated with a specific body part or usage type.
   */
  public static final TagKey<Item> CHARM = createItemTag("charm");

  /** Universal items that are able to equip or be equipped into any slot type. */
  public static final TagKey<Item> CURIO = createItemTag("curio");

  /** Items worn on the hands, such as gloves or gauntlets. */
  public static final TagKey<Item> HANDS = createItemTag("hands");

  /** Items worn on top of the head, such as crowns or hats. */
  public static final TagKey<Item> HEAD = createItemTag("head");

  /** Items worn around the neck, such as amulets or necklaces. */
  public static final TagKey<Item> NECKLACE = createItemTag("necklace");

  /** Items worn on the fingers, such as rings. */
  public static final TagKey<Item> RING = createItemTag("ring");

  /**
   * Creates an item tag key using the Curios namespace that is associated with the slot for the
   * given identifier.
   *
   * @param id The slot identifier
   * @return An item tag key
   */
  public static TagKey<Item> createItemTag(String id) {
    return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("curios", id));
  }
}
