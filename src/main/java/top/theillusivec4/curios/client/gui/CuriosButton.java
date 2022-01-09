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

package top.theillusivec4.curios.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;
import top.theillusivec4.curios.common.network.client.CPacketOpenVanilla;

public class CuriosButton extends ImageButton {

  private final AbstractContainerScreen<?> parentGui;
  private boolean isRecipeBookVisible = false;

  CuriosButton(AbstractContainerScreen<?> parentGui, int xIn, int yIn, int widthIn, int heightIn,
               int textureOffsetX, int textureOffsetY, int yDiffText, ResourceLocation resource) {

    super(xIn, yIn, widthIn, heightIn, textureOffsetX, textureOffsetY, yDiffText, resource,
        (button) -> {
          Minecraft mc = Minecraft.getInstance();

          if (mc.player != null) {
            ItemStack stack = mc.player.containerMenu.getCarried();
            mc.player.containerMenu.setCarried(ItemStack.EMPTY);

            if (parentGui instanceof CuriosScreen) {
              InventoryScreen inventory = new InventoryScreen(mc.player);
              mc.setScreen(inventory);
              mc.player.containerMenu.setCarried(stack);
              NetworkHandler.INSTANCE
                  .send(PacketDistributor.SERVER.noArg(), new CPacketOpenVanilla(stack));
            } else {

              if (parentGui instanceof InventoryScreen inventory) {
                RecipeBookComponent recipeBookGui = inventory.getRecipeBookComponent();

                if (recipeBookGui.isVisible()) {
                  recipeBookGui.toggleVisibility();
                }
              }
              NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                  new CPacketOpenCurios(stack));
            }
          }
        });
    this.parentGui = parentGui;
  }

  @Override
  public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY,
                     float partialTicks) {
    Tuple<Integer, Integer> offsets =
        CuriosScreen.getButtonOffset(parentGui instanceof CreativeModeInventoryScreen);
    x = parentGui.getGuiLeft() + offsets.getA();
    int yOffset = parentGui instanceof CreativeModeInventoryScreen ? 68 : 83;
    y = parentGui.getGuiTop() + offsets.getB() + yOffset;

    if (parentGui instanceof CreativeModeInventoryScreen gui) {
      boolean isInventoryTab = gui.getSelectedTab() == CreativeModeTab.TAB_INVENTORY.getId();
      this.active = isInventoryTab;

      if (!isInventoryTab) {
        return;
      }
    }
    super.render(matrixStack, mouseX, mouseY, partialTicks);
  }
}
