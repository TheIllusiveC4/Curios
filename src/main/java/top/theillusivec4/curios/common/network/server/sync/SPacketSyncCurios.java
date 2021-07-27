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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;

public class SPacketSyncCurios {

  private int entityId;
  private int entrySize;
  private Map<String, ICurioStacksHandler> map;

  public SPacketSyncCurios(int entityId, Map<String, ICurioStacksHandler> map) {
    this.entityId = entityId;
    this.entrySize = map.size();
    this.map = map;
  }

  public static void encode(SPacketSyncCurios msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.entityId);
    buf.writeInt(msg.entrySize);

    for (Map.Entry<String, ICurioStacksHandler> entry : msg.map.entrySet()) {
      buf.writeUtf(entry.getKey());
      buf.writeNbt(entry.getValue().serializeNBT());
    }
  }

  public static SPacketSyncCurios decode(FriendlyByteBuf buf) {
    int entityId = buf.readInt();
    int entrySize = buf.readInt();
    Map<String, ICurioStacksHandler> map = new LinkedHashMap<>();

    for (int i = 0; i < entrySize; i++) {
      String key = buf.readUtf(25);
      CurioStacksHandler stacksHandler = new CurioStacksHandler();
      CompoundTag compound = buf.readNbt();

      if (compound != null) {
        stacksHandler.deserializeNBT(compound);
      }
      map.put(key, stacksHandler);
    }
    return new SPacketSyncCurios(entityId, map);
  }

  public static void handle(SPacketSyncCurios msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ClientLevel world = Minecraft.getInstance().level;

      if (world != null) {
        Entity entity = world.getEntity(msg.entityId);

        if (entity instanceof LivingEntity) {
          CuriosApi.getCuriosHelper().getCuriosHandler((LivingEntity) entity)
              .ifPresent(handler -> handler.setCurios(msg.map));
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
