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
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.CuriosConstants;

public record SPacketGrabbedItem(ItemStack stack) implements CustomPacketPayload {

  public static final ResourceLocation ID =
      new ResourceLocation(CuriosConstants.MOD_ID, "grabbed_item");

  public SPacketGrabbedItem(final FriendlyByteBuf buf) {
    this(buf.readItem());
  }

  @Override
  public void write(@Nonnull FriendlyByteBuf buf) {
    buf.writeItem(this.stack());
  }

  @Nonnull
  @Override
  public ResourceLocation id() {
    return ID;
  }
}
