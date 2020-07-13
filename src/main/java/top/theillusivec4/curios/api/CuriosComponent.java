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

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import net.minecraft.util.Identifier;
import top.theillusivec4.curios.api.type.component.ICurio;
import top.theillusivec4.curios.api.type.component.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.component.IRenderableCurio;

public class CuriosComponent {

  public static final ComponentType<ICurio> ITEM = ComponentRegistry.INSTANCE
      .registerIfAbsent(new Identifier(CuriosApi.MODID, "item"), ICurio.class);

  public static final ComponentType<IRenderableCurio> ITEM_RENDER = ComponentRegistry.INSTANCE
      .registerIfAbsent(new Identifier(CuriosApi.MODID, "item_render"), IRenderableCurio.class);

  public static final ComponentType<ICuriosItemHandler> INVENTORY = ComponentRegistry.INSTANCE
      .registerIfAbsent(new Identifier(CuriosApi.MODID, "inventory"), ICuriosItemHandler.class);
}
