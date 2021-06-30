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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

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

  public static void encode(SPacketSyncOperation msg, PacketBuffer buf) {
    buf.writeInt(msg.entityId);
    buf.writeString(msg.curioId);
    buf.writeInt(msg.operation);
    buf.writeInt(msg.amount);
    buf.writeBoolean(msg.visible);
    buf.writeBoolean(msg.cosmetic);
  }

  public static SPacketSyncOperation decode(PacketBuffer buf) {
    return new SPacketSyncOperation(buf.readInt(), buf.readString(25),
        Operation.fromValue(buf.readInt()), buf.readInt(), buf.readBoolean(), buf.readBoolean());
  }

  public static void handle(SPacketSyncOperation msg, Supplier<NetworkEvent.Context> ctx) {

    ctx.get().enqueueWork(() -> {
      ClientWorld world = Minecraft.getInstance().world;

      if (world != null) {
        Entity entity = world.getEntityByID(msg.entityId);

        if (entity instanceof LivingEntity) {
          CuriosApi.getCuriosHelper().getCuriosHandler((LivingEntity) entity).ifPresent(handler -> {
            Operation op = Operation.fromValue(msg.operation);
            String id = msg.curioId;
            int amount = msg.amount;

            switch (op) {
              case GROW:
                handler.growSlotType(id, amount);
                break;
              case SHRINK:
                handler.shrinkSlotType(id, amount);
                break;
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
