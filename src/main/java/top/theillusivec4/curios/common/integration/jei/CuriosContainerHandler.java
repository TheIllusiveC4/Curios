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

package top.theillusivec4.curios.common.integration.jei;

import java.util.List;
import javax.annotation.Nonnull;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;
import top.theillusivec4.curios.client.screen.CuriosScreen;
import top.theillusivec4.curios.common.integration.CuriosExclusionAreas;

public class CuriosContainerHandler implements IGuiContainerHandler<CuriosScreen> {

  @Override
  @Nonnull
  public List<Rect2i> getGuiExtraAreas(@Nonnull CuriosScreen screen) {
    return CuriosExclusionAreas.create(screen);
  }
}

