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

package top.theillusivec4.curios.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.BracketDumper;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import org.openzen.zencode.java.ZenCodeType;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Collection;
import java.util.stream.Collectors;

@ZenRegister
@Document("mods/Curios/CuriosBracketDumpers")
@ZenCodeType.Name("mods.curios.CuriosBracketDumpers")
public class CuriosBracketDumpers {
    @ZenCodeType.Method
    @BracketDumper("curiosslottype")
    public static Collection<String> getSlotTypeDump() {
        return CuriosApi.getSlotHelper().getSlotTypeIds().stream()
                .map(s -> "<curiosslottype:" + s + ">")
                .collect(Collectors.toList());
    }
}
