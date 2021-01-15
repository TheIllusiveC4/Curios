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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.component.ICurio;
import top.theillusivec4.curios.client.screen.CuriosScreen;
import top.theillusivec4.curios.common.CuriosNetwork;
import top.theillusivec4.curios.common.inventory.screen.CuriosScreenHandler;
import top.theillusivec4.curios.server.CurioArgumentType;

public class CuriosClientNetwork {

  public static void registerPackets() {
    ClientSidePacketRegistry.INSTANCE
        .register(CuriosNetwork.BREAK, (((packetContext, packetByteBuf) -> {
          int entityId = packetByteBuf.readInt();
          String curioId = packetByteBuf.readString(25);
          int index = packetByteBuf.readInt();

          packetContext.getTaskQueue().execute(() -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client != null && client.world != null) {
              Entity entity = client.world.getEntityById(entityId);

              if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {
                  ItemStack stack = handler.getStacksHandler(curioId)
                      .map(stacksHandler -> stacksHandler.getStacks().getStack(index))
                      .orElse(ItemStack.EMPTY);
                  Optional<ICurio> maybeCurio = CuriosApi.getCuriosHelper().getCurio(stack);
                  maybeCurio.ifPresent(curio -> curio.curioBreak(stack, livingEntity));

                  if (!maybeCurio.isPresent()) {
                    ICurio.playBreakAnimation(stack, livingEntity);
                  }
                });
              }
            }
          });
        })));

    ClientSidePacketRegistry.INSTANCE
        .register(CuriosNetwork.SET_ICONS, (((packetContext, packetByteBuf) -> {
          int entrySize = packetByteBuf.readInt();
          Map<String, Identifier> map = new HashMap<>();

          for (int i = 0; i < entrySize; i++) {
            map.put(packetByteBuf.readString(25), new Identifier(packetByteBuf.readString(100)));
          }
          packetContext.getTaskQueue().execute(() -> {
            Set<String> slotIds = new HashSet<>();
            CuriosApi.getIconHelper().clearIcons();

            for (Map.Entry<String, Identifier> entry : map.entrySet()) {
              CuriosApi.getIconHelper().addIcon(entry.getKey(), entry.getValue());
              slotIds.add(entry.getKey());
            }
            CurioArgumentType.slotIds = slotIds;
          });
        })));

    ClientSidePacketRegistry.INSTANCE
        .register(CuriosNetwork.GRAB_ITEM, (((packetContext, packetByteBuf) -> {
          ItemStack stack = packetByteBuf.readItemStack();
          packetContext.getTaskQueue().execute(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity clientPlayerEntity = client.player;

            if (clientPlayerEntity != null) {
              clientPlayerEntity.inventory.setCursorStack(stack);
            }
          });
        })));

    ClientSidePacketRegistry.INSTANCE
        .register(CuriosNetwork.SCROLL, (((packetContext, packetByteBuf) -> {
          int syncId = packetByteBuf.readInt();
          int scrollIndex = packetByteBuf.readInt();

          packetContext.getTaskQueue().execute(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity clientPlayerEntity = client.player;
            Screen screen = client.currentScreen;

            if (clientPlayerEntity != null) {
              ScreenHandler screenHandler = clientPlayerEntity.currentScreenHandler;

              if (screenHandler instanceof CuriosScreenHandler && screenHandler.syncId == syncId) {
                ((CuriosScreenHandler) screenHandler).scrollToIndex(scrollIndex);
              }
            }

            if (screen instanceof CuriosScreen) {
              ((CuriosScreen) screen).updateRenderButtons();
            }
          });
        })));
  }
}
