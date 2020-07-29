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
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.client.KeyRegistry;
import top.theillusivec4.curios.common.CuriosNetwork;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

  @Inject(at = @At("RETURN"), method = "keyPressed", cancellable = true)
  public void _curios_keyPressed(int keyCode, int scanCode, int modifiers,
      CallbackInfoReturnable<Boolean> cb) {

    if (KeyRegistry.openCurios.matchesKey(keyCode, scanCode)) {
      ClientSidePacketRegistry.INSTANCE
          .sendToServer(CuriosNetwork.OPEN_CURIOS, new PacketByteBuf(Unpooled.buffer()));
      cb.setReturnValue(true);
    }
  }
}
