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
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

/**
 * Collection of tags used by Curios.
 */
public final class CuriosTags {

  /**
   * Items worn on the back, such as capes or backpacks.
   */
  public static final TagKey<Item> BACK = createItemTag(CuriosSlotTypes.Preset.BACK);

  /**
   * Items worn around the waist, such as belts or pouches.
   */
  public static final TagKey<Item> BELT = createItemTag(CuriosSlotTypes.Preset.BELT);

  /**
   * Items worn on the torso/chest, such as cloaks or shirts.
   */
  public static final TagKey<Item> BODY = createItemTag(CuriosSlotTypes.Preset.BODY);

  /**
   * Items worn around the wrist, such as bands or bracelets.
   */
  public static final TagKey<Item> BRACELET = createItemTag(CuriosSlotTypes.Preset.BRACELET);

  /**
   * Miscellaneous items that are not strongly associated with a specific body part or usage type.
   */
  public static final TagKey<Item> CHARM = createItemTag(CuriosSlotTypes.Preset.CHARM);

  /**
   * Universal items that are able to equip or be equipped into any slot type.
   */
  public static final TagKey<Item> CURIO = createItemTag(CuriosSlotTypes.Preset.CURIO);

  /**
   * Items worn on the feet, such as shoes or boots.
   */
  public static final TagKey<Item> FEET = createItemTag(CuriosSlotTypes.Preset.FEET);

  /**
   * Items worn on the hands, such as gloves or gauntlets.
   */
  public static final TagKey<Item> HANDS = createItemTag(CuriosSlotTypes.Preset.HANDS);

  /**
   * Items worn on top of the head, such as crowns or hats.
   */
  public static final TagKey<Item> HEAD = createItemTag(CuriosSlotTypes.Preset.HEAD);

  /**
   * Items worn around the neck, such as amulets or necklaces.
   */
  public static final TagKey<Item> NECKLACE = createItemTag(CuriosSlotTypes.Preset.NECKLACE);

  /**
   * Items worn on the fingers, such as rings.
   */
  public static final TagKey<Item> RING = createItemTag(CuriosSlotTypes.Preset.RING);

  /**
   * Items that are to be specifically excluded from being accepted by the universal curio slot.
   *
   * <p>This is not the same concept as the one related to the {@link #CURIO} item tag. That tag is
   * for universal items being accepted into any slot type. This tag is related to universal slots
   * that can accept any curio item, which is what this tag is excluding items from.
   */
  public static final TagKey<Item> GENERIC_EXCLUSIONS = createItemTag("generic_exclusions");

  /**
   * Entity types that should be treated like players (including players themselves) for the
   * purposes of curio item classification, such as armor stands.
   */
  public static final TagKey<EntityType<?>> PLAYER_LIKE = createEntityTypeTag("player_like");

  /**
   * Creates an entity type tag key using the Curios namespace.
   *
   * @param id The path for the tag.
   * @return An entity type tag key.
   */
  public static TagKey<EntityType<?>> createEntityTypeTag(String id) {
    return TagKey.create(Registries.ENTITY_TYPE, CuriosResources.resource(id));
  }

  /**
   * Creates an item tag key using the Curios namespace and associated with the given
   * {@link CuriosSlotTypes.Preset}.
   *
   * <p>The path of each preset will be consistent with the result of
   * {@link CuriosSlotTypes.Preset#id()}.
   *
   * @param preset The slot identifier
   * @return An item tag key.
   */
  public static TagKey<Item> createItemTag(CuriosSlotTypes.Preset preset) {
    return createItemTag(preset.id());
  }

  /**
   * Creates an item tag key using the Curios namespace.
   *
   * @param id The path for the tag.
   * @return An item tag key.
   */
  public static TagKey<Item> createItemTag(String id) {
    return TagKey.create(Registries.ITEM, CuriosResources.resource(id));
  }
}
