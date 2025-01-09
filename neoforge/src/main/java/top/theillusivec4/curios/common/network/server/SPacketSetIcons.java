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

package top.theillusivec4.curios.common.network.server;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.curios.CuriosConstants;

public class SPacketSetIcons implements CustomPacketPayload {

  public static final Type<SPacketSetIcons> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "set_icons"));
  public static final StreamCodec<RegistryFriendlyByteBuf, SPacketSetIcons> STREAM_CODEC =
      new StreamCodec<>() {
        @Nonnull
        @Override
        public SPacketSetIcons decode(@Nonnull RegistryFriendlyByteBuf buf) {
          return new SPacketSetIcons(buf);
        }

        @Override
        public void encode(@Nonnull RegistryFriendlyByteBuf buf, SPacketSetIcons packet) {
          buf.writeInt(packet.entrySize);

          for (Map.Entry<String, ResourceLocation> entry : packet.map.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeUtf(entry.getValue().toString());
          }
        }
      };

  private final int entrySize;
  public final Map<String, ResourceLocation> map;

  public SPacketSetIcons(Map<String, ResourceLocation> map) {
    this.entrySize = map.size();
    this.map = map;
  }

  public SPacketSetIcons(final FriendlyByteBuf buf) {
    int entrySize = buf.readInt();
    Map<String, ResourceLocation> map = new HashMap<>();

    for (int i = 0; i < entrySize; i++) {
      map.put(buf.readUtf(), ResourceLocation.parse(buf.readUtf()));
    }
    this.entrySize = map.size();
    this.map = map;
  }

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
