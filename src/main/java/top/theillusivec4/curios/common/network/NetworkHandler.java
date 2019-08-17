/*
 * Copyright (C) 2018-2019  C4
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

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.common.network.client.CPacketDestroyCurios;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;
import top.theillusivec4.curios.common.network.client.CPacketOpenVanilla;
import top.theillusivec4.curios.common.network.client.CPacketScrollCurios;
import top.theillusivec4.curios.common.network.server.SPacketBreakCurio;
import top.theillusivec4.curios.common.network.server.SPacketScrollCurios;
import top.theillusivec4.curios.common.network.server.sync.*;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkHandler {

  private static final String PTC_VERSION = "1";

  public static final SimpleChannel INSTANCE =
      NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Curios.MODID, "main"))
                                    .networkProtocolVersion(() -> PTC_VERSION)
                                    .clientAcceptedVersions(PTC_VERSION::equals)
                                    .serverAcceptedVersions(PTC_VERSION::equals)
                                    .simpleChannel();

  private static int id = 0;

  public static void register() {

    //Client Packets
    registerMessage(CPacketOpenCurios.class, CPacketOpenCurios::encode, CPacketOpenCurios::decode,
                    CPacketOpenCurios::handle);
    registerMessage(CPacketOpenVanilla.class, CPacketOpenVanilla::encode,
                    CPacketOpenVanilla::decode, CPacketOpenVanilla::handle);
    registerMessage(CPacketScrollCurios.class, CPacketScrollCurios::encode,
                    CPacketScrollCurios::decode, CPacketScrollCurios::handle);
    registerMessage(CPacketDestroyCurios.class, CPacketDestroyCurios::encode,
                    CPacketDestroyCurios::decode, CPacketDestroyCurios::handle);

    // Server Packets
    registerMessage(SPacketSyncContents.class, SPacketSyncContents::encode,
                    SPacketSyncContents::decode, SPacketSyncContents::handle);
    registerMessage(SPacketScrollCurios.class, SPacketScrollCurios::encode,
                    SPacketScrollCurios::decode, SPacketScrollCurios::handle);
    registerMessage(SPacketSyncActive.class, SPacketSyncActive::encode, SPacketSyncActive::decode,
                    SPacketSyncActive::handle);
    registerMessage(SPacketSyncSize.class, SPacketSyncSize::encode, SPacketSyncSize::decode,
                    SPacketSyncSize::handle);
    registerMessage(SPacketSyncMap.class, SPacketSyncMap::encode, SPacketSyncMap::decode,
                    SPacketSyncMap::handle);
    registerMessage(SPacketSyncContentsWithTag.class, SPacketSyncContentsWithTag::encode,
                    SPacketSyncContentsWithTag::decode, SPacketSyncContentsWithTag::handle);
    registerMessage(SPacketBreakCurio.class, SPacketBreakCurio::encode, SPacketBreakCurio::decode,
                    SPacketBreakCurio::handle);
  }

  private static <MSG> void registerMessage(Class<MSG> messageType,
                                            BiConsumer<MSG, PacketBuffer> encoder,
                                            Function<PacketBuffer, MSG> decoder,
                                            BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {

    INSTANCE.registerMessage(id++, messageType, encoder, decoder, messageConsumer);
  }
}
