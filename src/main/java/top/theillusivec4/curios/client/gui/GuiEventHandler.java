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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketDestroy;

public class GuiEventHandler {

  @SubscribeEvent
  public void onInventoryGuiInit(GuiScreenEvent.InitGuiEvent.Post evt) {
    Screen screen = evt.getGui();

    if (screen instanceof InventoryScreen || screen instanceof CreativeScreen) {
      ContainerScreen<?> gui = (ContainerScreen<?>) screen;
      boolean isCreative = screen instanceof CreativeScreen;
      Tuple<Integer, Integer> offsets = CuriosScreen.getButtonOffset(isCreative);
      int x = offsets.getA();
      int y = offsets.getB();
      int size = isCreative ? 10 : 14;
      int textureOffsetX = isCreative ? 64 : 50;
      evt.addWidget(new CuriosButton(gui, gui.getGuiLeft() + x, gui.getGuiTop() + y + 83, size,
              size, textureOffsetX, 0, size, CuriosScreen.CURIO_INVENTORY));
    }
  }

  @SubscribeEvent
  public void onInventoryGuiDrawBackground(GuiScreenEvent.DrawScreenEvent.Pre evt) {

    if (!(evt.getGui() instanceof InventoryScreen)) {
      return;
    }
    InventoryScreen gui = (InventoryScreen) evt.getGui();

    gui.oldMouseX = evt.getMouseX();
    gui.oldMouseY = evt.getMouseY();
  }

  @SubscribeEvent
  public void onMouseClick(GuiScreenEvent.MouseClickedEvent.Pre evt) {
    long handle = Minecraft.getInstance().getMainWindow().getHandle();
    boolean isLeftShiftDown = InputMappings.isKeyDown(handle, GLFW.GLFW_KEY_LEFT_SHIFT);
    boolean isRightShiftDown = InputMappings.isKeyDown(handle, GLFW.GLFW_KEY_RIGHT_SHIFT);
    boolean isShiftDown = isLeftShiftDown || isRightShiftDown;

    if (!(evt.getGui() instanceof CreativeScreen) || !isShiftDown) {
      return;
    }

    CreativeScreen gui = (CreativeScreen) evt.getGui();

    if (gui.getSelectedTabIndex() != ItemGroup.INVENTORY.getIndex()) {
      return;
    }
    Slot destroyItemSlot = gui.destroyItemSlot;
    Slot slot = gui.getSelectedSlot(evt.getMouseX(), evt.getMouseY());

    if (destroyItemSlot != null && slot == destroyItemSlot) {
      NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new CPacketDestroy());
    }
  }
}