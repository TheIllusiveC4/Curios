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

package top.theillusivec4.curios.common.network.server;

import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.curios.CuriosConstants;

public record SPacketBreak(int entityId, String curioId, int slotId) implements
    CustomPacketPayload {

  public static final ResourceLocation ID = new ResourceLocation(CuriosConstants.MOD_ID, "break");

  public SPacketBreak(final FriendlyByteBuf buf) {
    this(buf.readInt(), buf.readUtf(), buf.readInt());
  }

  @Override
  public void write(@Nonnull FriendlyByteBuf buf) {
    buf.writeInt(this.entityId());
    buf.writeUtf(this.curioId());
    buf.writeInt(this.slotId());
  }

  @Nonnull
  @Override
  public ResourceLocation id() {
    return ID;
  }
}
