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

import com.mojang.blaze3d.platform.InputConstants;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosResources;
import top.theillusivec4.curios.client.screen.CuriosScreen;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;
import top.theillusivec4.curios.common.network.client.CPacketOpenVanilla;

public class CuriosButton extends ImageButton {

  public static final WidgetSprites BIG =
      new WidgetSprites(CuriosResources.resource("button"),
                        CuriosResources.resource("button_highlighted"));
  public static final WidgetSprites SMALL =
      new WidgetSprites(CuriosResources.resource("button_small"),
                        CuriosResources.resource("button_small_highlighted"));
  private final AbstractContainerScreen<?> parentGui;

  public CuriosButton(AbstractContainerScreen<?> parentGui, int xIn, int yIn, int widthIn,
                      int heightIn,
                      WidgetSprites sprites) {
    super(xIn, yIn, widthIn, heightIn, sprites,
          (button) -> {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player != null) {
              ItemStack stack = mc.player.containerMenu.getCarried();
              mc.player.containerMenu.setCarried(ItemStack.EMPTY);

              if (parentGui instanceof CuriosScreen curiosScreen) {
                mc.mouseHandler.mouseGrabbed = true;
                mc.player.clientSideCloseContainer();
                InventoryScreen inventoryScreen = new InventoryScreen(mc.player);
                mc.mouseHandler.mouseGrabbed = false;
                mc.setScreen(inventoryScreen);
                inventoryScreen.xMouse = curiosScreen.oldMouseX;
                inventoryScreen.yMouse = curiosScreen.oldMouseY;
                mc.player.inventoryMenu.setCarried(stack);
                PacketDistributor.sendToServer(new CPacketOpenVanilla(stack));
              } else {

                if (parentGui instanceof InventoryScreen inventory) {
                  RecipeBookComponent<?> recipeBookGui = inventory.recipeBookComponent;

                  if (recipeBookGui.isVisible()) {
                    recipeBookGui.toggleVisibility();
                  }
                }
                PacketDistributor.sendToServer(new CPacketOpenCurios(stack));
              }
            }
          });
    this.parentGui = parentGui;
  }

  @Override
  public void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY,
                           float partialTicks) {
    Tuple<Integer, Integer> offsets =
        CuriosScreen.getButtonOffset(parentGui instanceof CreativeModeInventoryScreen);
    this.setX(parentGui.getGuiLeft() + offsets.getA() + 2);
    int yOffset = parentGui instanceof CreativeModeInventoryScreen ? 70 : 85;
    this.setY(parentGui.getGuiTop() + offsets.getB() + yOffset);

    if (parentGui instanceof CreativeModeInventoryScreen gui) {
      boolean isInventoryTab = gui.isInventoryOpen();
      this.active = isInventoryTab;

      if (!isInventoryTab) {
        return;
      }
    }
    super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
  }
}
