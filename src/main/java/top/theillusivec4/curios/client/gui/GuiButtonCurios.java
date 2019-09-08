/*
 * Copyright (C) 2018-2019  C4
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

package top.theillusivec4.curios.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.PacketDistributor;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;
import top.theillusivec4.curios.common.network.client.CPacketOpenVanilla;

public class GuiButtonCurios extends ImageButton {

  private final ContainerScreen parentGui;
  private       boolean         isRecipeBookVisible = false;

  GuiButtonCurios(ContainerScreen parentGui, int xIn, int yIn, int widthIn, int heightIn,
                  int textureOffestX, int textureOffestY, int yDiffText,
                  ResourceLocation resource) {

    super(xIn, yIn, widthIn, heightIn, textureOffestX, textureOffestY, yDiffText, resource,
          (button) -> {
            Minecraft mc = Minecraft.getInstance();

            if (parentGui instanceof CuriosScreen) {
              InventoryScreen inventory = new InventoryScreen(mc.player);
              mc.displayGuiScreen(inventory);
              NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                                           new CPacketOpenVanilla());
            } else {
              NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                                           new CPacketOpenCurios(0, 0));
            }
          });
    this.parentGui = parentGui;
  }

  @Override
  public void render(int mouseX, int mouseY, float partialTicks) {

    if (parentGui instanceof InventoryScreen) {
      boolean lastVisible = isRecipeBookVisible;
      isRecipeBookVisible = ((InventoryScreen) parentGui).getRecipeGui().isVisible();

      if (lastVisible != isRecipeBookVisible) {
        this.setPosition(parentGui.getGuiLeft() + 26, parentGui.height / 2 - 75);
      }
    }
    super.render(mouseX, mouseY, partialTicks);
  }
}
