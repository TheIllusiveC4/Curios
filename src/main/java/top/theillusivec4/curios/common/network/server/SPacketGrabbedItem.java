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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class SPacketGrabbedItem {

  private final ItemStack stack;

  public SPacketGrabbedItem(ItemStack stackIn) {
    this.stack = stackIn;
  }

  public static void encode(SPacketGrabbedItem msg, FriendlyByteBuf buf) {
    buf.writeItem(msg.stack);
  }

  public static SPacketGrabbedItem decode(FriendlyByteBuf buf) {
    return new SPacketGrabbedItem(buf.readItem());
  }

  public static void handle(SPacketGrabbedItem msg, Supplier<NetworkEvent.Context> ctx) {

    ctx.get().enqueueWork(() -> {
      LocalPlayer clientPlayer = Minecraft.getInstance().player;

      if (clientPlayer != null) {
        clientPlayer.containerMenu.setCarried(msg.stack);
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
