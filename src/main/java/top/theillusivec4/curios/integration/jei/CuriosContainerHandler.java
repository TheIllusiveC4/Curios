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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.recipebook.RecipeTabToggleWidget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.client.gui.CuriosScreen;

public class CuriosContainerHandler implements IGuiContainerHandler<CuriosScreen> {

  private static final Field RECIPE_TABS = ObfuscationReflectionHelper
      .findField(RecipeBookGui.class, "field_193018_j");

  @Override
  @Nonnull
  public List<Rectangle2d> getGuiExtraAreas(CuriosScreen containerScreen) {
    ClientPlayerEntity player = containerScreen.getMinecraft().player;

    if (player != null) {
      return CuriosApi.getCuriosHelper().getCuriosHandler(containerScreen.getMinecraft().player)
          .map(handler -> {
            List<Rectangle2d> areas = new ArrayList<>();
            int slotCount = handler.getSlots();
            int width = slotCount > 8 ? 42 : 26;

            if (containerScreen.getContainer().hasCosmeticColumn()) {
              width += 18;
            }
            int height = 7 + slotCount * 18;
            int left = containerScreen.getGuiLeft() - width;
            int top = containerScreen.getGuiTop() + 4;
            areas.add(new Rectangle2d(left, top, width, height));
            RecipeBookGui guiRecipeBook = containerScreen.getRecipeGui();

            if (guiRecipeBook.isVisible()) {
              int i =
                  (containerScreen.field_230708_k_ - 147) / 2 - (containerScreen.widthTooNarrow ? 0
                      : 86);
              int j = (containerScreen.field_230709_l_ - 166) / 2;
              areas.add(new Rectangle2d(i, j, 147, 166));
              try {
                @SuppressWarnings("unchecked") List<RecipeTabToggleWidget> tabs = (List<RecipeTabToggleWidget>) RECIPE_TABS
                    .get(guiRecipeBook);

                for (RecipeTabToggleWidget tab : tabs) {

                  if (tab.field_230694_p_) {
                    areas.add(new Rectangle2d(tab.field_230690_l_, tab.field_230691_m_,
                        tab.func_230998_h_(), tab.getHeight()));
                  }
                }
              } catch (IllegalAccessException e) {
                Curios.LOGGER.error("Error accessing recipe tabs!");
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
