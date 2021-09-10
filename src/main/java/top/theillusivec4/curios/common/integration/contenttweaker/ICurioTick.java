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

import com.blamejared.contenttweaker.api.functions.ICotFunction;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.entity.LivingEntity;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister(modDeps = "contenttweaker")
@FunctionalInterface
@Document("mods/Curios/ContentTweaker/ICurioTick")
@ZenCodeType.Name("mods.curios.contenttweaker.ICurioTick")
public interface ICurioTick extends ICotFunction {
    @ZenCodeType.Method
    void tick(String identifier, int index, LivingEntity wearer);
}
