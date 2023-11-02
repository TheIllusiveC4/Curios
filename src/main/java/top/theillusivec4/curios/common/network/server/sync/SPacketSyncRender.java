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

package top.theillusivec4.curios.common.network.server.sync;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

public class SPacketSyncRender {

  private int entityId;
  private int slotId;
  private String curioId;
  private boolean value;

  public SPacketSyncRender(int entityId, String curioId, int slotId, boolean value) {
    this.entityId = entityId;
    this.slotId = slotId;
    this.curioId = curioId;
    this.value = value;
  }

  public static void encode(SPacketSyncRender msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.entityId);
    buf.writeUtf(msg.curioId);
    buf.writeInt(msg.slotId);
    buf.writeBoolean(msg.value);
  }

  public static SPacketSyncRender decode(FriendlyByteBuf buf) {
    return new SPacketSyncRender(buf.readInt(), buf.readUtf(), buf.readInt(),
        buf.readBoolean());
  }

  public static void handle(SPacketSyncRender msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ClientLevel world = Minecraft.getInstance().level;

      if (world != null) {
        Entity entity = world.getEntity(msg.entityId);

        if (entity instanceof LivingEntity) {
          CuriosApi.getCuriosInventory((LivingEntity) entity).ifPresent(
              handler -> handler.getStacksHandler(msg.curioId).ifPresent(stacksHandler -> {
                int index = msg.slotId;
                NonNullList<Boolean> renderStatuses = stacksHandler.getRenders();

                if (renderStatuses.size() > index) {
                  renderStatuses.set(index, msg.value);
                }
              }));
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
