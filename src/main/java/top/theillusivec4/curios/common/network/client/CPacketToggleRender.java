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
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncRender;

public class CPacketToggleRender {

  String id;
  int index;

  public CPacketToggleRender(String id, int index) {
    this.id = id;
    this.index = index;
  }

  public static void encode(CPacketToggleRender msg, FriendlyByteBuf buf) {
    buf.writeUtf(msg.id);
    buf.writeInt(msg.index);
  }

  public static CPacketToggleRender decode(FriendlyByteBuf buf) {
    return new CPacketToggleRender(buf.readUtf(100), buf.readInt());
  }

  public static void handle(CPacketToggleRender msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();

      if (sender != null) {
        CuriosApi.getCuriosHelper().getCuriosHandler(sender)
            .ifPresent(handler -> handler.getStacksHandler(msg.id).ifPresent(stacksHandler -> {
              NonNullList<Boolean> renderStatuses = stacksHandler.getRenders();

              if (renderStatuses.size() > msg.index) {
                boolean value = !renderStatuses.get(msg.index);
                renderStatuses.set(msg.index, value);
                NetworkHandler.INSTANCE
                    .send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sender),
                        new SPacketSyncRender(sender.getId(), msg.id, msg.index, value));
              }
            }));
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
