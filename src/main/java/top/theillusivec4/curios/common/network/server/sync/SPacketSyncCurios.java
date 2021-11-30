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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;

public class SPacketSyncCurios {

  private int entityId;
  private int entrySize;
  private Map<String, CompoundNBT> map;

  public SPacketSyncCurios(int entityId, Map<String, ICurioStacksHandler> map) {
    Map<String, CompoundNBT> result = new LinkedHashMap<>();

    for (Map.Entry<String, ICurioStacksHandler> entry : map.entrySet()) {
      result.put(entry.getKey(), entry.getValue().getSyncTag());
    }
    this.entityId = entityId;
    this.entrySize = map.size();
    this.map = result;
  }

  public SPacketSyncCurios(Map<String, CompoundNBT> map, int entityId) {
    this.entityId = entityId;
    this.entrySize = map.size();
    this.map = map;
  }

  public static void encode(SPacketSyncCurios msg, PacketBuffer buf) {
    buf.writeInt(msg.entityId);
    buf.writeInt(msg.entrySize);

    for (Map.Entry<String, CompoundNBT> entry : msg.map.entrySet()) {
      buf.writeString(entry.getKey());
      buf.writeCompoundTag(entry.getValue());
    }
  }

  public static SPacketSyncCurios decode(PacketBuffer buf) {
    int entityId = buf.readInt();
    int entrySize = buf.readInt();
    Map<String, CompoundNBT> map = new LinkedHashMap<>();

    for (int i = 0; i < entrySize; i++) {
      String key = buf.readString(25);
      map.put(key, buf.readCompoundTag());
    }
    return new SPacketSyncCurios(map, entityId);
  }

  public static void handle(SPacketSyncCurios msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ClientWorld world = Minecraft.getInstance().world;

      if (world != null) {
        Entity entity = world.getEntityByID(msg.entityId);

        if (entity instanceof LivingEntity) {
          CuriosApi.getCuriosHelper().getCuriosHandler((LivingEntity) entity)
              .ifPresent(handler -> {
                Map<String, ICurioStacksHandler> stacks = new LinkedHashMap<>();

                for (Map.Entry<String, CompoundNBT> entry : msg.map.entrySet()) {
                  ICurioStacksHandler stacksHandler =
                      new CurioStacksHandler(handler, entry.getKey());
                  stacksHandler.applySyncTag(entry.getValue());
                  stacks.put(entry.getKey(), stacksHandler);
                }
                handler.setCurios(stacks);
              });
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
