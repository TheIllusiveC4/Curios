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

package top.theillusivec4.curios.client;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.client.render.CuriosRenderComponents;
import top.theillusivec4.curios.client.screen.CuriosScreen;
import top.theillusivec4.curios.common.CuriosNetwork;
import top.theillusivec4.curios.common.CuriosRegistry;

public class CuriosClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    CuriosApi.setIconHelper(new IconHelper());
    KeyRegistry.registerKeys();
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      if (KeyRegistry.openCurios.wasPressed() && MinecraftClient.getInstance().isWindowFocused()) {
        ClientSidePacketRegistry.INSTANCE
            .sendToServer(CuriosNetwork.OPEN_CURIOS, new PacketByteBuf(Unpooled.buffer()));
      }
    });
    ScreenRegistry.register(CuriosRegistry.CURIOS_SCREENHANDLER, CuriosScreen::new);
    ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
        .register(((spriteAtlasTexture, registry) -> {
          for (SlotTypePreset preset : SlotTypePreset.values()) {
            registry.register(
                new Identifier(CuriosApi.MODID, "item/empty_" + preset.getIdentifier() + "_slot"));
          }
          registry.register(new Identifier(CuriosApi.MODID, "item/empty_cosmetic_slot"));
          registry.register(new Identifier(CuriosApi.MODID, "item/empty_curio_slot"));
        }));
    CuriosRenderComponents.register();
    CuriosClientNetwork.registerPackets();
  }
}
