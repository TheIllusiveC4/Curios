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

package top.theillusivec4.curios.common.network;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.network.client.CPacketDestroy;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;
import top.theillusivec4.curios.common.network.client.CPacketOpenVanilla;
import top.theillusivec4.curios.common.network.client.CPacketScroll;
import top.theillusivec4.curios.common.network.client.CPacketToggleRender;
import top.theillusivec4.curios.common.network.server.SPacketBreak;
import top.theillusivec4.curios.common.network.server.SPacketGrabbedItem;
import top.theillusivec4.curios.common.network.server.SPacketScroll;
import top.theillusivec4.curios.common.network.server.SPacketSetIcons;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncCurios;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncModifiers;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncOperation;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncRender;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack;

public class NetworkHandler {

  private static final String PTC_VERSION = "1";

  public static SimpleChannel INSTANCE;

  private static int id = 0;

  public static void register() {

    INSTANCE = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Curios.MODID, "main"))
        .networkProtocolVersion(() -> PTC_VERSION).clientAcceptedVersions(PTC_VERSION::equals)
        .serverAcceptedVersions(PTC_VERSION::equals).simpleChannel();

    //Client Packets
    register(CPacketOpenCurios.class, CPacketOpenCurios::encode, CPacketOpenCurios::decode,
        CPacketOpenCurios::handle);
    register(CPacketOpenVanilla.class, CPacketOpenVanilla::encode, CPacketOpenVanilla::decode,
        CPacketOpenVanilla::handle);
    register(CPacketScroll.class, CPacketScroll::encode, CPacketScroll::decode,
        CPacketScroll::handle);
    register(CPacketDestroy.class, CPacketDestroy::encode, CPacketDestroy::decode,
        CPacketDestroy::handle);
    register(CPacketToggleRender.class, CPacketToggleRender::encode, CPacketToggleRender::decode,
        CPacketToggleRender::handle);

    // Server Packets
    register(SPacketSyncStack.class, SPacketSyncStack::encode, SPacketSyncStack::decode,
        SPacketSyncStack::handle);
    register(SPacketScroll.class, SPacketScroll::encode, SPacketScroll::decode,
        SPacketScroll::handle);
    register(SPacketSyncOperation.class, SPacketSyncOperation::encode, SPacketSyncOperation::decode,
        SPacketSyncOperation::handle);
    register(SPacketSyncCurios.class, SPacketSyncCurios::encode, SPacketSyncCurios::decode,
        SPacketSyncCurios::handle);
    register(SPacketBreak.class, SPacketBreak::encode, SPacketBreak::decode, SPacketBreak::handle);
    register(SPacketGrabbedItem.class, SPacketGrabbedItem::encode, SPacketGrabbedItem::decode,
        SPacketGrabbedItem::handle);
    register(SPacketSetIcons.class, SPacketSetIcons::encode, SPacketSetIcons::decode,
        SPacketSetIcons::handle);
    register(SPacketSyncRender.class, SPacketSyncRender::encode, SPacketSyncRender::decode,
        SPacketSyncRender::handle);
    register(SPacketSyncModifiers.class, SPacketSyncModifiers::encode, SPacketSyncModifiers::decode,
        SPacketSyncModifiers::handle);

    // Assignment of curio breaking to the network instance
    CuriosApi.getCuriosHelper().setBrokenCurioConsumer((slotContext) -> INSTANCE
        .send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(slotContext::entity),
            new SPacketBreak(slotContext.entity().getId(), slotContext.identifier(),
                slotContext.index())));
  }

  private static <M> void register(Class<M> messageType, BiConsumer<M, FriendlyByteBuf> encoder,
                                   Function<FriendlyByteBuf, M> decoder,
                                   BiConsumer<M, Supplier<NetworkEvent.Context>> messageConsumer) {
    INSTANCE.registerMessage(id++, messageType, encoder, decoder, messageConsumer);
  }
}
