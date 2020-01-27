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

package top.theillusivec4.curios.integration.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.Rectangle2d;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.client.gui.CuriosScreen;

public class CuriosContainerHandler implements IGuiContainerHandler<CuriosScreen> {

  @Override
  @Nonnull
  public List<Rectangle2d> getGuiExtraAreas(CuriosScreen containerScreen) {
    ClientPlayerEntity player = containerScreen.getMinecraft().player;

    if (player != null) {
      return CuriosAPI.getCuriosHandler(containerScreen.getMinecraft().player).map(handler -> {
        List<Rectangle2d> areas = new ArrayList<>();
        int slotCount = handler.getSlots();
        int width = slotCount > 8 ? 42 : 26;
        int height = 7 + slotCount * 18;
        int left = containerScreen.getGuiLeft() - width;
        int top = containerScreen.getGuiTop() + 4;
        areas.add(new Rectangle2d(left, top, width, height));
        return areas;
      }).orElse(Collections.emptyList());
    } else {
      return Collections.emptyList();
    }
  }
}
