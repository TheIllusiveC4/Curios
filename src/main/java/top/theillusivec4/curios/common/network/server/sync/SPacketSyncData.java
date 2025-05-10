/*
 * Copyright (c) 2018-2024 C4
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
 *
 */

package top.theillusivec4.curios.common.network.server.sync;

import javax.annotation.Nonnull;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosResources;
import top.theillusivec4.curios.common.data.CuriosSlotResources;

public record SPacketSyncData(CuriosSlotResources slotResources)
    implements CustomPacketPayload {

  public static final Type<SPacketSyncData> TYPE = new Type<>(CuriosResources.resource("sync_data"));
  public static final StreamCodec<RegistryFriendlyByteBuf, SPacketSyncData> STREAM_CODEC =
      StreamCodec.composite(
          CuriosSlotResources.STREAM_CODEC,
          SPacketSyncData::slotResources,
          SPacketSyncData::new
      );

  public static void send(ServerPlayer player) {
    PacketDistributor.sendToPlayer(player, new SPacketSyncData(CuriosSlotResources.SERVER));
  }

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
