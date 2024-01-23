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

import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.CuriosConstants;

public record SPacketSyncStack(int entityId, String curioId, int slotId, ItemStack stack,
                               int handlerType, CompoundTag compoundTag) implements
    CustomPacketPayload {

  public static final ResourceLocation ID =
      new ResourceLocation(CuriosConstants.MOD_ID, "sync_stack");

  public SPacketSyncStack(final FriendlyByteBuf buf) {
    this(buf.readInt(), buf.readUtf(), buf.readInt(), buf.readItem(), buf.readInt(), buf.readNbt());
  }

  @Override
  public void write(@Nonnull FriendlyByteBuf buf) {
    buf.writeInt(this.entityId());
    buf.writeUtf(this.curioId());
    buf.writeInt(this.slotId());
    buf.writeItem(this.stack());
    buf.writeInt(this.handlerType());
    buf.writeNbt(this.compoundTag());
  }

  @Nonnull
  @Override
  public ResourceLocation id() {
    return ID;
  }

  public enum HandlerType {
    EQUIPMENT, COSMETIC;

    public static HandlerType fromValue(int value) {
      try {
        return HandlerType.values()[value];
      } catch (ArrayIndexOutOfBoundsException e) {
        throw new IllegalArgumentException("Unknown handler value: " + value);
      }
    }
  }
}
