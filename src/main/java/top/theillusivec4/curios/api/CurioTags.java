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

import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

/**
 * A reference class for storing a list of commonly used tags to avoid modders implementing
 * duplicate slots. If there is a tag that is commonly used that isn't already in this list, please
 * submit a PR or issue ticket on GitHub.
 */
public final class CurioTags {

  /**
   * Used for curios that can be worn anywhere.
   */
  public static final Tag<Item> CURIO = tag("curio");

  /**
   * Used for capes, backpacks, or anything worn on the back.
   */
  public static final Tag<Item> BACK = tag("back");

  /**
   * Used for belts or anything worn around the waist.
   */
  public static final Tag<Item> BELT = tag("belt");

  /**
   * Used for cloaks, mantles, shirts, or anything worn around the torso.
   */
  public static final Tag<Item> BODY = tag("body");

  /**
   * Used for miscellaneous items that do not belong to any particular type.
   */
  public static final Tag<Item> CHARM = tag("charm");

  /**
   * Used for crowns, hats, or anything worn on top of the head.
   */
  public static final Tag<Item> HEAD = tag("head");

  /**
   * Used for gloves, gauntlets, or anything worn on hands.
   */
  public static final Tag<Item> HANDS = tag("hands");

  /**
   * Used for amulets, necklaces, or anything worn around the neck.
   */
  public static final Tag<Item> NECKLACE = tag("necklace");

  /**
   * Used for rings or anything worn on the "fingers".
   */
  public static final Tag<Item> RING = tag("ring");

  private static Tag<Item> tag(String name) {

    return new ItemTags.Wrapper(new ResourceLocation(CuriosAPI.MODID, name));
  }
}
