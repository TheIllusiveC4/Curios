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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.curios.CuriosConstants;

public class SPacketSyncData implements CustomPacketPayload {

  public static final Type<SPacketSyncData> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "sync_data"));
  public static final StreamCodec<RegistryFriendlyByteBuf, SPacketSyncData> STREAM_CODEC =
      new StreamCodec<>() {
        @Nonnull
        @Override
        public SPacketSyncData decode(@Nonnull RegistryFriendlyByteBuf buf) {
          return new SPacketSyncData(buf);
        }

        @Override
        public void encode(@Nonnull RegistryFriendlyByteBuf buf, SPacketSyncData packet) {
          CompoundTag tag = new CompoundTag();
          tag.put("SlotData", packet.slotData);
          tag.put("EntityData", packet.entityData);
          buf.writeNbt(tag);
        }
      };

  public final ListTag slotData;
  public final ListTag entityData;

  public SPacketSyncData(ListTag slotData, ListTag entityData) {
    this.slotData = slotData;
    this.entityData = entityData;
  }

  public SPacketSyncData(final FriendlyByteBuf buf) {
    CompoundTag tag = buf.readNbt();

    if (tag != null) {
      this.slotData = tag.getList("SlotData", Tag.TAG_COMPOUND);
      this.entityData = tag.getList("EntityData", Tag.TAG_COMPOUND);
    } else {
      this.slotData = new ListTag();
      this.entityData = new ListTag();
    }
  }

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
