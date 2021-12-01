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

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketDestroy;

public class GuiEventHandler {

  @SubscribeEvent
  public void onInventoryGuiInit(ScreenEvent.InitScreenEvent.Post evt) {
    Screen screen = evt.getScreen();

    if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {
      AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>) screen;
      boolean isCreative = screen instanceof CreativeModeInventoryScreen;
      Tuple<Integer, Integer> offsets = CuriosScreen.getButtonOffset(isCreative);
      int x = offsets.getA();
      int y = offsets.getB();
      int size = isCreative ? 10 : 14;
      int textureOffsetX = isCreative ? 64 : 50;
      int yOffset = isCreative ? 68 : 83;
      evt.addListener(new CuriosButton(gui, gui.getGuiLeft() + x, gui.getGuiTop() + y + yOffset, size,
          size, textureOffsetX, 0, size, CuriosScreen.CURIO_INVENTORY));
    }
  }

  @SubscribeEvent
  public void onInventoryGuiDrawBackground(ScreenEvent.DrawScreenEvent.Pre evt) {

    if (!(evt.getScreen() instanceof InventoryScreen gui)) {
      return;
    }
    gui.xMouse = evt.getMouseX();
    gui.yMouse = evt.getMouseY();
  }

  @SubscribeEvent
  public void onMouseClick(ScreenEvent.MouseClickedEvent.Pre evt) {
    long handle = Minecraft.getInstance().getWindow().getWindow();
    boolean isLeftShiftDown = InputConstants.isKeyDown(handle, GLFW.GLFW_KEY_LEFT_SHIFT);
    boolean isRightShiftDown = InputConstants.isKeyDown(handle, GLFW.GLFW_KEY_RIGHT_SHIFT);
    boolean isShiftDown = isLeftShiftDown || isRightShiftDown;

    if (!(evt.getScreen() instanceof CreativeModeInventoryScreen gui) || !isShiftDown) {
      return;
    }

    if (gui.getSelectedTab() != CreativeModeTab.TAB_INVENTORY.getId()) {
      return;
    }
    Slot destroyItemSlot = gui.destroyItemSlot;
    Slot slot = gui.findSlot(evt.getMouseX(), evt.getMouseY());

    if (destroyItemSlot != null && slot == destroyItemSlot) {
      NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new CPacketDestroy());
    }
  }
}