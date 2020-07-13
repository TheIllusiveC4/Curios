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

package top.theillusivec4.curios.mixin;

import io.netty.buffer.Unpooled;
import java.util.Collection;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.common.CuriosNetwork;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

  @Inject(method = "onPlayerConnect", at = @At("TAIL"))
  public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player,
      CallbackInfo cb) {
    Collection<ISlotType> slotTypes = CuriosApi.getSlotHelper().getSlotTypes();
    PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
    packetByteBuf.writeInt(slotTypes.size());
    slotTypes.forEach(type -> {
      packetByteBuf.writeString(type.getIdentifier());
      packetByteBuf.writeString(type.getIcon().toString());
    });
    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, CuriosNetwork.SET_ICONS, packetByteBuf);
  }
}
