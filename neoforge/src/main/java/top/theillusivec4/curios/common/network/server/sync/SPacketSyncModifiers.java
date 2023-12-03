/*
 * Copyright (c) 2018-2023 C4
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
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.SlotModifiersUpdatedEvent;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

public class SPacketSyncModifiers {

  private int entityId;
  private int entrySize;
  private Map<String, CompoundTag> updates;

  public SPacketSyncModifiers(int entityId, Set<ICurioStacksHandler> updates) {
    Map<String, CompoundTag> result = new LinkedHashMap<>();

    for (ICurioStacksHandler stacksHandler : updates) {
      result.put(stacksHandler.getIdentifier(), stacksHandler.getSyncTag());
    }
    this.entityId = entityId;
    this.entrySize = result.size();
    this.updates = result;
  }

  public SPacketSyncModifiers(Map<String, CompoundTag> map, int entityId) {
    this.entityId = entityId;
    this.entrySize = map.size();
    this.updates = map;
  }

  public static void encode(SPacketSyncModifiers msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.entityId);
    buf.writeInt(msg.entrySize);

    for (Map.Entry<String, CompoundTag> entry : msg.updates.entrySet()) {
      buf.writeUtf(entry.getKey());
      buf.writeNbt(entry.getValue());
    }
  }

  public static SPacketSyncModifiers decode(FriendlyByteBuf buf) {
    int entityId = buf.readInt();
    int entrySize = buf.readInt();
    Map<String, CompoundTag> map = new LinkedHashMap<>();

    for (int i = 0; i < entrySize; i++) {
      String key = buf.readUtf();
      map.put(key, buf.readNbt());
    }
    return new SPacketSyncModifiers(map, entityId);
  }

  public static void handle(SPacketSyncModifiers msg, NetworkEvent.Context ctx) {
    ctx.enqueueWork(() -> {
      ClientLevel world = Minecraft.getInstance().level;

      if (world != null) {
        Entity entity = world.getEntity(msg.entityId);

        if (entity instanceof LivingEntity livingEntity) {
          CuriosApi.getCuriosInventory(livingEntity)
              .ifPresent(handler -> {
                Map<String, ICurioStacksHandler> curios = handler.getCurios();

                for (Map.Entry<String, CompoundTag> entry : msg.updates.entrySet()) {
                  String id = entry.getKey();
                  ICurioStacksHandler stacksHandler = curios.get(id);

                  if (stacksHandler != null) {
                    stacksHandler.applySyncTag(entry.getValue());
                  }
                }

                if (!msg.updates.isEmpty()) {
                  NeoForge.EVENT_BUS.post(
                      new SlotModifiersUpdatedEvent(livingEntity, msg.updates.keySet()));
                }

                if (entity instanceof LocalPlayer player) {

                  if (player.containerMenu instanceof CuriosContainer) {
                    ((CuriosContainer) player.containerMenu).resetSlots();
                  }
                }
              });
        }
      }
    });
    ctx.setPacketHandled(true);
  }
}
