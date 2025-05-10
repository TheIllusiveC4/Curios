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

package top.theillusivec4.curios.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.config.CuriosClientConfig;
import top.theillusivec4.curios.client.screen.button.CuriosButton;
import top.theillusivec4.curios.common.network.client.CPacketDestroy;

public class CuriosScreenEvents {

  @SubscribeEvent
  public void postScreenInit(final ScreenEvent.Init.Post evt) {
    Screen screen = evt.getScreen();

    if (!CuriosClientConfig.CLIENT.enableButton.get()) {
      return;
    }

    if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {
      AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>) screen;
      boolean isCreative = screen instanceof CreativeModeInventoryScreen;
      Tuple<Integer, Integer> offsets = CuriosScreen.getButtonOffset(isCreative);
      int x = offsets.getA();
      int y = offsets.getB();
      int size = isCreative ? 8 : 10;
      int yOffset = isCreative ? 67 : 81;
      evt.addListener(
          new CuriosButton(gui, gui.getGuiLeft() + x - 2, gui.getGuiTop() + y + yOffset, size, size,
                           isCreative ? CuriosButton.SMALL : CuriosButton.BIG));
    }
  }

  @SubscribeEvent
  public void preMouseClick(final ScreenEvent.MouseButtonPressed.Pre evt) {
    long handle = Minecraft.getInstance().getWindow().getWindow();
    boolean isLeftShiftDown = InputConstants.isKeyDown(handle, GLFW.GLFW_KEY_LEFT_SHIFT);
    boolean isRightShiftDown = InputConstants.isKeyDown(handle, GLFW.GLFW_KEY_RIGHT_SHIFT);
    boolean isShiftDown = isLeftShiftDown || isRightShiftDown;

    if (!(evt.getScreen() instanceof CreativeModeInventoryScreen gui) || !isShiftDown
        || !gui.isInventoryOpen()) {
      return;
    }
    Slot destroyItemSlot = gui.destroyItemSlot;
    Slot slot = gui.getSlotUnderMouse();

    if (destroyItemSlot != null && slot == destroyItemSlot) {
      PacketDistributor.sendToServer(new CPacketDestroy());
    }
  }
}