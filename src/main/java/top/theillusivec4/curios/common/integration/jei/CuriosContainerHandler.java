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

package top.theillusivec4.curios.common.integration.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.renderer.Rect2i;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.client.gui.CuriosScreen;

public class CuriosContainerHandler implements IGuiContainerHandler<CuriosScreen> {

  @Override
  @Nonnull
  public List<Rect2i> getGuiExtraAreas(CuriosScreen containerScreen) {
    LocalPlayer player = containerScreen.getMinecraft().player;

    if (player != null) {
      return CuriosApi.getCuriosInventory(containerScreen.getMinecraft().player)
          .map(handler -> {
            List<Rect2i> areas = new ArrayList<>();
            int slotCount = handler.getVisibleSlots();

            if (slotCount <= 0) {
              return areas;
            }
            int width = slotCount > 8 ? 42 : 26;

            if (containerScreen.getMenu().hasCosmeticColumn()) {
              width += 18;
            }
            int height = 7 + slotCount * 18;
            int left = containerScreen.getGuiLeft() - width;
            int top = containerScreen.getGuiTop() + 4;
            areas.add(new Rect2i(left, top, width, height));
            RecipeBookComponent guiRecipeBook = containerScreen.getRecipeBookComponent();

            if (guiRecipeBook.isVisible()) {
              int i = (containerScreen.width - 147) / 2 - (containerScreen.widthTooNarrow ? 0 : 86);
              int j = (containerScreen.height - 166) / 2;
              areas.add(new Rect2i(i, j, 147, 166));

              for (RecipeBookTabButton tab : guiRecipeBook.tabButtons) {
                if (tab.active) {
                  areas.add(new Rect2i(tab.getX(), tab.getX(), tab.getWidth(), tab.getHeight()));
                }
              }
              return areas;
            }
            return areas;
          }).orElse(Collections.emptyList());
    } else {
      return Collections.emptyList();
    }
  }
}
