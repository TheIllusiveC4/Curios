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

package top.theillusivec4.curios.common.network.server;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

public class SPacketScroll {

  private int windowId;
  private int index;

  public SPacketScroll(int windowId, int index) {
    this.windowId = windowId;
    this.index = index;
  }

  public static void encode(SPacketScroll msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.windowId);
    buf.writeInt(msg.index);
  }

  public static SPacketScroll decode(FriendlyByteBuf buf) {
    return new SPacketScroll(buf.readInt(), buf.readInt());
  }

  public static void handle(SPacketScroll msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer clientPlayer = mc.player;
      Screen screen = mc.screen;

      if (clientPlayer != null) {
        AbstractContainerMenu container = clientPlayer.containerMenu;

        if (container instanceof CuriosContainer && container.containerId == msg.windowId) {
          ((CuriosContainer) container).scrollToIndex(msg.index);
        }
      }

      if (screen instanceof CuriosScreen) {
        ((CuriosScreen) screen).updateRenderButtons();
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
