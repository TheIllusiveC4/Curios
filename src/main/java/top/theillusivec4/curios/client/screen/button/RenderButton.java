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

package top.theillusivec4.curios.client.screen.button;

import javax.annotation.Nonnull;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.curios.common.inventory.CurioSlot;

public class RenderButton extends ImageButton implements ICuriosWidget {

  public static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
      ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled"),
      ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled_highlighted")
  );
  private final ResourceLocation resourceLocation;
  private final int yTexStart;
  private final int xTexStart;
  private final CurioSlot slot;

  public RenderButton(CurioSlot slot, int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn,
                      int yTexStartIn, ResourceLocation resourceLocationIn,
                      OnPress onPressIn) {
    super(xIn, yIn, widthIn, heightIn, BUTTON_SPRITES, onPressIn);
    this.resourceLocation = resourceLocationIn;
    this.yTexStart = yTexStartIn;
    this.xTexStart = xTexStartIn;
    this.slot = slot;
  }

  @Override
  public void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY,
                           float partialTicks) {
    // NO-OP
  }

  public void renderButtonOverlay(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY,
                                  float partialTicks) {
    int j = this.xTexStart;

    if (!slot.getRenderStatus()) {
      j += 8;
    }
    guiGraphics.blit(RenderType::guiTextured, this.resourceLocation, this.getX(), this.getY(),
                     (float) j, (float) this.yTexStart, this.width, this.height, 256, 256);
  }
}
