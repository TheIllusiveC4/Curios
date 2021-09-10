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

package top.theillusivec4.curios.common.integration.contenttweaker;

import com.blamejared.crafttweaker_annotations.annotations.Document;
import org.openzen.zencode.java.ZenCodeType;

// Hack class to solve some super classes are not found by crafttweaker annotation processor
@SuppressWarnings("unused")
class AnnotationProcessorDummies {
    @ZenCodeType.Name("mods.contenttweaker.item.advance.CoTItemAdvanced")
    @Document("mods/contenttweaker/API/item/advance/CoTItemAdvanced")
    private static class AdvancedItem {}

    @Document("mods/contenttweaker/API/item/ItemTypeBuilder")
    @ZenCodeType.Name("mods.contenttweaker.item.ItemTypeBuilder")
    private static class ItemTypeBuilder {}
}
