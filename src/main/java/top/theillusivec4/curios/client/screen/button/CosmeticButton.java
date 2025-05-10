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
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosResources;
import top.theillusivec4.curios.client.screen.CuriosScreen;
import top.theillusivec4.curios.common.network.client.CPacketToggleCosmetics;

public class CosmeticButton extends ImageButton implements ICuriosWidget {

  public static final WidgetSprites OFF =
      new WidgetSprites(CuriosResources.resource("cosmetic_off"),
                        CuriosResources.resource("cosmetic_off_highlighted"));
  public static final WidgetSprites ON =
      new WidgetSprites(CuriosResources.resource("cosmetic_on"),
                        CuriosResources.resource("cosmetic_on_highlighted"));

  private final CuriosScreen parentGui;

  public CosmeticButton(CuriosScreen parentGui, int xIn, int yIn, int widthIn, int heightIn) {
    super(xIn, yIn, widthIn, heightIn, OFF,
          (button) -> {
            parentGui.getMenu().toggleCosmetics();
            PacketDistributor.sendToServer(
                new CPacketToggleCosmetics(parentGui.getMenu().containerId));
          });
    this.parentGui = parentGui;
    this.setTooltip(Tooltip.create(Component.translatable("gui.curios.toggle.cosmetics")));
  }

  @Override
  public void renderWidget(@Nonnull GuiGraphics guiGraphics, int x, int y, float partialTicks) {
    WidgetSprites sprites1;

    if (this.parentGui.getMenu().isViewingCosmetics) {
      sprites1 = ON;
    } else {
      sprites1 = OFF;
    }
    this.setX(this.parentGui.getGuiLeft() - 27);
    this.setY(this.parentGui.getGuiTop() - 18);
    ResourceLocation resourcelocation = sprites1.get(this.isActive(), this.isHoveredOrFocused());
    guiGraphics.blitSprite(RenderType::guiTextured, resourcelocation, this.getX(), this.getY(),
                           this.width, this.height);
  }
}
