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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import top.theillusivec4.curios.common.data.CuriosEntityManager;

public class SPacketSyncData {

  private final ListTag data;

  public SPacketSyncData(ListTag data) {
    this.data = data;
  }

  public static void encode(SPacketSyncData msg, FriendlyByteBuf buf) {
    CompoundTag tag = new CompoundTag();
    tag.put("Data", msg.data);
    buf.writeNbt(tag);
  }

  public static SPacketSyncData decode(FriendlyByteBuf buf) {
    CompoundTag tag = buf.readNbt();

    if (tag != null) {
      return new SPacketSyncData(tag.getList("Data", Tag.TAG_COMPOUND));
    }
    return new SPacketSyncData(new ListTag());
  }

  public static void handle(SPacketSyncData msg, CustomPayloadEvent.Context ctx) {
    ctx.enqueueWork(() -> CuriosEntityManager.applySyncPacket(msg.data));
    ctx.setPacketHandled(true);
  }
}
