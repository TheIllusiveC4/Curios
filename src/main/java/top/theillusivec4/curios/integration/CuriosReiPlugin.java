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

package top.theillusivec4.curios.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.BaseBoundsHandler;
import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.client.screen.CuriosScreen;
import top.theillusivec4.curios.mixin.IHandledScreenAccessor;

@Environment(EnvType.CLIENT)
public class CuriosReiPlugin implements REIPluginV0 {

  @Override
  public Identifier getPluginIdentifier() {
    return new Identifier(CuriosApi.MODID + ":plugin");
  }

  @Override
  public void registerBounds(DisplayHelper displayHelper) {
    BaseBoundsHandler baseBoundsHandler = BaseBoundsHandler.getInstance();
    baseBoundsHandler.registerExclusionZones(CuriosScreen.class, () -> {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      CuriosScreen screen = (CuriosScreen) MinecraftClient.getInstance().currentScreen;

      if (player != null && screen != null) {
        return CuriosApi.getCuriosHelper().getCuriosHandler(player).map(handler -> {
          List<Rectangle> areas = new ArrayList<>();
          int slotCount = handler.getVisibleSlots();

          if (slotCount <= 0) {
            return areas;
          }
          int width = slotCount > 8 ? 42 : 26;

          if (screen.getScreenHandler().hasCosmeticColumn()) {
            width += 18;
          }
          int height = 7 + slotCount * 18;
          int left = ((IHandledScreenAccessor) screen).getX() - width;
          int top = ((IHandledScreenAccessor) screen).getY() + 4;
          areas.add(new Rectangle(left, top, width, height));
          return areas;
        }).orElse(Collections.emptyList());
      } else {
        return Collections.emptyList();
      }
    });
  }
}
