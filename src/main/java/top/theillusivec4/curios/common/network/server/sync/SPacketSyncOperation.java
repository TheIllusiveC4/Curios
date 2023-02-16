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

import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.SlotModifiersUpdatedEvent;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

public class SPacketSyncOperation {

  private int entityId;
  private String curioId;
  private int operation;
  private int amount;
  private boolean visible;
  private boolean cosmetic;

  public SPacketSyncOperation(int entityId, String curioId, Operation operation) {
    this(entityId, curioId, operation, 0);
  }

  public SPacketSyncOperation(int entityId, String curioId, Operation operation, int amount) {
    this(entityId, curioId, operation, amount, true, false);
  }

  public SPacketSyncOperation(int entityId, String curioId, Operation operation, int amount,
                              boolean visible, boolean cosmetic) {
    this.entityId = entityId;
    this.curioId = curioId;
    this.amount = amount;
    this.operation = operation.ordinal();
    this.visible = visible;
    this.cosmetic = cosmetic;
  }

  public static void encode(SPacketSyncOperation msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.entityId);
    buf.writeUtf(msg.curioId);
    buf.writeInt(msg.operation);
    buf.writeInt(msg.amount);
    buf.writeBoolean(msg.visible);
    buf.writeBoolean(msg.cosmetic);
  }

  public static SPacketSyncOperation decode(FriendlyByteBuf buf) {
    return new SPacketSyncOperation(buf.readInt(), buf.readUtf(25),
        Operation.fromValue(buf.readInt()), buf.readInt(), buf.readBoolean(), buf.readBoolean());
  }

  public static void handle(SPacketSyncOperation msg, Supplier<NetworkEvent.Context> ctx) {

    ctx.get().enqueueWork(() -> {
      ClientLevel world = Minecraft.getInstance().level;

      if (world != null) {
        Entity entity = world.getEntity(msg.entityId);

        if (entity instanceof LivingEntity livingEntity) {
          CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {
            Operation op = Operation.fromValue(msg.operation);
            String id = msg.curioId;
            int amount = msg.amount;

            switch (op) {
              case GROW -> handler.growSlotType(id, amount);
              case SHRINK -> handler.shrinkSlotType(id, amount);
            }
            MinecraftForge.EVENT_BUS.post(new SlotModifiersUpdatedEvent(livingEntity, Set.of(id)));

            if (entity instanceof Player player) {

              if (player.containerMenu instanceof CuriosContainer) {
                ((CuriosContainer) player.containerMenu).resetSlots();
              }
            }
          });
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }

  public enum Operation {
    SHRINK, GROW;

    public static Operation fromValue(int value) {
      try {
        return Operation.values()[value];
      } catch (ArrayIndexOutOfBoundsException e) {
        throw new IllegalArgumentException("Unknown operation value: " + value);
      }
    }
  }
}
