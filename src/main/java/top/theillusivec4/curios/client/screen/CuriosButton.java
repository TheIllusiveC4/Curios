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

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import top.theillusivec4.curios.common.CuriosNetwork;
import top.theillusivec4.curios.mixin.IHandledScreenAccessor;
import top.theillusivec4.curios.mixin.IInventoryScreenAccessor;

public class CuriosButton extends TexturedButtonWidget {

  private final HandledScreen<?> parentGui;
  private boolean isRecipeBookOpen = false;

  public CuriosButton(HandledScreen<?> parentGui, int xIn, int yIn, int widthIn, int heightIn,
      int textureOffsetX, int textureOffsetY, int yDiffText, Identifier identifier) {

    super(xIn, yIn, widthIn, heightIn, textureOffsetX, textureOffsetY, yDiffText, identifier,
        (button) -> {
          MinecraftClient mc = MinecraftClient.getInstance();

          if (parentGui instanceof CuriosScreen && mc.player != null) {
            InventoryScreen inventory = new InventoryScreen(mc.player);
            ItemStack stack = mc.player.inventory.getCursorStack();
            mc.player.inventory.setCursorStack(ItemStack.EMPTY);
            float mouseX = ((CuriosScreen) parentGui).currentMouseX;
            float mouseY = ((CuriosScreen) parentGui).currentMouseY;
            mc.openScreen(inventory);
            @SuppressWarnings("ConstantConditions") IInventoryScreenAccessor accessor = (IInventoryScreenAccessor) inventory;
            accessor.setMouseX(mouseX);
            accessor.setMouseY(mouseY);
            mc.player.inventory.setCursorStack(stack);
            ClientSidePacketRegistry.INSTANCE
                .sendToServer(CuriosNetwork.OPEN_VANILLA, new PacketByteBuf(Unpooled.buffer()));
          } else {

            if (parentGui instanceof InventoryScreen) {
              InventoryScreen inventory = (InventoryScreen) parentGui;
              RecipeBookWidget recipeBookGui = inventory.getRecipeBookWidget();

              if (recipeBookGui.isOpen()) {
                recipeBookGui.toggleOpen();
              }
            }
            ClientSidePacketRegistry.INSTANCE
                .sendToServer(CuriosNetwork.OPEN_CURIOS, new PacketByteBuf(Unpooled.buffer()));
          }
        });
    this.parentGui = parentGui;
  }

  @Override
  public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

    if (parentGui instanceof InventoryScreen) {
      boolean lastOpen = isRecipeBookOpen;
      isRecipeBookOpen = ((InventoryScreen) parentGui).getRecipeBookWidget().isOpen();

      if (lastOpen != isRecipeBookOpen) {
        Pair<Integer, Integer> offsets = CuriosScreen.getButtonOffset(false);
        this.setPos(((IHandledScreenAccessor) parentGui).getX() + offsets.getLeft(),
            parentGui.height / 2 + offsets.getRight());
      }
    } else if (parentGui instanceof CreativeInventoryScreen) {
      CreativeInventoryScreen gui = (CreativeInventoryScreen) parentGui;
      boolean isInventoryTab = gui.getSelectedTab() == ItemGroup.INVENTORY.getIndex();
      this.active = isInventoryTab;

      if (!isInventoryTab) {
        return;
      }
    }
    super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
  }
}
