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
import top.theillusivec4.curios.CuriosConstants;

public class CuriosTags {

    public static final TagKey<Item> BACK =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "back"));
    public static final TagKey<Item> BELT =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "belt"));
    public static final TagKey<Item> BODY =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "body"));
    public static final TagKey<Item> BRACELET =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "bracelet"));
    public static final TagKey<Item> CHARM =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "charm"));
    public static final TagKey<Item> CURIO =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "curio"));
    public static final TagKey<Item> HANDS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "hands"));
    public static final TagKey<Item> HEAD =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "head"));
    public static final TagKey<Item> NECKLACE =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "necklace"));
    public static final TagKey<Item> RING =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "ring"));
}
