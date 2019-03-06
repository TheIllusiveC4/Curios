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

package top.theillusivec4.curios.api;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import top.theillusivec4.curios.Curios;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CuriosRegistry {

    private static Map<String, CurioType> idToType = Maps.newHashMap();
    private static Map<String, Tag<Item>> idToTag = Maps.newHashMap();
    private static Map<Item, Set<String>> itemToTypes = Maps.newHashMap();

    static Map<String, ResourceLocation> icons = Maps.newHashMap();

    public static CurioType getOrRegisterType(@Nonnull String identifier) {

        if (identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be empty!");
        }
        return idToType.merge(identifier, new CurioType(identifier), (key, value) -> value);
    }

    @Nullable
    public static CurioType getType(String identifier) {
        return idToType.get(identifier);
    }

    public static ImmutableSet<String> getTypeIdentifiers() { return ImmutableSet.copyOf(idToType.keySet()); }

    public static ImmutableSet<String> getCurioTags(Item item) {

        if (idToTag.isEmpty()) {
            refreshTags();
        }

        if (itemToTypes.containsKey(item)) {
            return ImmutableSet.copyOf(itemToTypes.get(item));
        } else {
            Set<String> tags = Sets.newHashSet();

            for (String identifier : idToTag.keySet()) {

                if (idToTag.get(identifier).contains(item)) {
                    tags.add(identifier);
                }
            }
            itemToTypes.put(item, tags);
            return ImmutableSet.copyOf(tags);
        }
    }

    public static ImmutableSet<ResourceLocation> getResources() {
        return ImmutableSet.copyOf(icons.values());
    }

    private static void refreshTags() {
        idToTag = ItemTags.getCollection().getTagMap().entrySet()
                .stream()
                .filter(map -> map.getKey().getNamespace().equals(Curios.MODID))
                .collect(Collectors.toMap(entry -> entry.getKey().getPath(), Map.Entry::getValue));
        itemToTypes.clear();
    }
}
