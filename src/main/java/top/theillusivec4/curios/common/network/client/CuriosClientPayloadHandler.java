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

package top.theillusivec4.curios.common.network.client;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.common.network.server.SPacketBreak;
import top.theillusivec4.curios.common.network.server.SPacketGrabbedItem;
import top.theillusivec4.curios.common.network.server.SPacketPage;
import top.theillusivec4.curios.common.network.server.SPacketQuickMove;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncActiveState;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncCurios;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncData;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncModifiers;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncRender;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack;

public class CuriosClientPayloadHandler {

  private static final CuriosClientPayloadHandler INSTANCE = new CuriosClientPayloadHandler();

  public static CuriosClientPayloadHandler getInstance() {
    return INSTANCE;
  }

  private static void handle(final IPayloadContext ctx, Runnable handler) {
    ctx.enqueueWork(handler)
        .exceptionally(e -> {
          ctx.disconnect(Component.translatable("curios.networking.failed"));
          return null;
        });
  }

  public void handle(final SPacketQuickMove data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketPage data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketBreak data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketSyncRender data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketSyncModifiers data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketSyncData data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketSyncActiveState data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketSyncCurios data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketGrabbedItem data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }

  public void handle(final SPacketSyncStack data, final IPayloadContext ctx) {
    handle(ctx, () -> CuriosClientPackets.handle(data));
  }
}
