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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.CuriosConstants;
import top.theillusivec4.curios.client.screen.CuriosScreen;
import top.theillusivec4.curios.common.network.client.CPacketPage;

public class PageButton extends Button implements ICuriosWidget {

  private final CuriosScreen parentGui;
  private final Type type;
  private static final ResourceLocation CURIO_INVENTORY =
      ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID,
                                            "textures/gui/curios/inventory.png");

  public PageButton(CuriosScreen parentGui, int xIn, int yIn, int widthIn, int heightIn,
                    Type type) {
    super(xIn, yIn, widthIn, heightIn, CommonComponents.EMPTY,
          (button) -> PacketDistributor.sendToServer(
              new CPacketPage(parentGui.getMenu().containerId, type == Type.NEXT)),
          DEFAULT_NARRATION);
    this.parentGui = parentGui;
    this.type = type;
  }

  @Override
  public void renderWidget(@Nonnull GuiGraphics guiGraphics, int x, int y, float partialTicks) {
    int xText = type == Type.NEXT ? 43 : 32;
    int yText = 25;

    if (type == Type.NEXT) {
      this.setX(this.parentGui.getGuiLeft() - 17);
      this.active = this.parentGui.getMenu().currentPage + 1 < this.parentGui.getMenu().totalPages;
    } else {
      this.setX(this.parentGui.getGuiLeft() - 28);
      this.active = this.parentGui.getMenu().currentPage > 0;
    }

    if (!this.isActive()) {
      yText += 12;
    } else if (this.isHoveredOrFocused()) {
      xText += 22;
    }

    if (this.isHovered()) {
      guiGraphics.renderTooltip(Minecraft.getInstance().font,
                                Component.translatable("gui.curios.page",
                                                       this.parentGui.getMenu().currentPage + 1,
                                                       this.parentGui.getMenu().totalPages), x, y);
    }
    guiGraphics.blit(RenderType::guiTextured, CURIO_INVENTORY, this.getX(), this.getY(), xText,
                     yText, this.width, this.height, 256, 256);
  }

  public enum Type {
    NEXT,
    PREVIOUS
  }
}
