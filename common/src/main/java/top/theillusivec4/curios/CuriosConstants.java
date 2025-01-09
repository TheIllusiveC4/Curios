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

package top.theillusivec4.curios;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CuriosConstants {

  public static final String MOD_ID = "curios";
  public static final String MOD_NAME = "Curios API";
  public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

  public static class Tags {

    public static final TagKey<Item> BACK =
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "back"));
    public static final TagKey<Item> BELT =
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "belt"));
    public static final TagKey<Item> BODY =
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "body"));
    public static final TagKey<Item> BRACELET =
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "bracelet"));
    public static final TagKey<Item> CHARM =
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "charm"));
    public static final TagKey<Item> CURIO =
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "curio"));
    public static final TagKey<Item> HANDS =
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "hands"));
    public static final TagKey<Item> HEAD =
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "head"));
    public static final TagKey<Item> NECKLACE =
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "necklace"));
    public static final TagKey<Item> RING =
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "ring"));
  }
}
