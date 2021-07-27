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

package top.theillusivec4.curios.common.network.client;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CPacketDestroy {

  public static void encode(CPacketDestroy msg, FriendlyByteBuf buf) {
  }

  public static CPacketDestroy decode(FriendlyByteBuf buf) {
    return new CPacketDestroy();
  }

  public static void handle(CPacketDestroy msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();

      if (sender != null) {
        CuriosApi.getCuriosHelper().getCuriosHandler(sender)
            .ifPresent(handler -> handler.getCurios().values().forEach(stacksHandler -> {
              IDynamicStackHandler stackHandler = stacksHandler.getStacks();
              IDynamicStackHandler cosmeticStackHandler = stacksHandler.getCosmeticStacks();

              for (int i = 0; i < stackHandler.getSlots(); i++) {
                stackHandler.setStackInSlot(i, ItemStack.EMPTY);
                cosmeticStackHandler.setStackInSlot(i, ItemStack.EMPTY);
              }
            }));
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
