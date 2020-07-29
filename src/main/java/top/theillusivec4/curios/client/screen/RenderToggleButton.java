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

package top.theillusivec4.curios.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import top.theillusivec4.curios.common.inventory.CurioSlot;

public class RenderToggleButton extends TexturedButtonWidget {

  private final Identifier identifier;
  private final int yTexStart;
  private final int xTexStart;
  private final CurioSlot slot;

  private boolean wasHovered = false;

  public RenderToggleButton(CurioSlot slot, int xIn, int yIn, int widthIn, int heightIn,
      int xTexStartIn, int yTexStartIn, int yDiffTextIn, Identifier identifier,
      ButtonWidget.PressAction onPressIn) {
    super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, identifier, 256, 256,
        onPressIn);
    this.identifier = identifier;
    this.yTexStart = yTexStartIn;
    this.xTexStart = xTexStartIn;
    this.slot = slot;
  }

  @Override
  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
    // NO-OP
  }

  public void renderButtonOverlay(MatrixStack matrixStack, int mouseX, int mouseY,
      float partialTicks) {
    MinecraftClient minecraft = MinecraftClient.getInstance();
    minecraft.getTextureManager().bindTexture(this.identifier);
    RenderSystem.disableDepthTest();
    int j = this.xTexStart;

    if (!slot.getRenderStatus()) {
      j += 8;
    }
    drawTexture(matrixStack, this.x, this.y, (float) j, (float) this.yTexStart, this.width,
        this.height, 256, 256);
    RenderSystem.enableDepthTest();
  }
}
