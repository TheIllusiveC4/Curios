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

import com.blamejared.crafttweaker.api.annotations.BracketResolver;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import org.openzen.zencode.java.ZenCodeType;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;

@ZenRegister
@Document("mods/Curios/CuriosBracketHandlers")
@ZenCodeType.Name("mods.curios.CuriosBracketHandlers")
public class CuriosBracketHandlers {
    /**
     * Gets the given {@link ISlotType}. Throws an Exception if not found.
     * @param token What you would write in the BEP call.
     * @return The found {@link ISlotType}
     * @docParam token "belt"
     */
    @ZenCodeType.Method
    @BracketResolver("curiosslottype")
    public static ISlotType getSlotType(String token) {
        return CuriosApi.getSlotHelper().getSlotType(token).orElseThrow(() ->
                new IllegalArgumentException("Could not get slot type with name: <curiosslottype:" + token + ">!"));
    }
}
