package top.theillusivec4.curios.common.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;
import top.theillusivec4.curios.common.network.client.CPacketOpenVanilla;
import top.theillusivec4.curios.common.network.client.CPacketScrollCurios;
import top.theillusivec4.curios.common.network.server.SPacketPlayEquip;
import top.theillusivec4.curios.common.network.server.SPacketSyncCurios;
import top.theillusivec4.curios.common.network.server.SPacketScrollCurios;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkHandler {

    private static final String PTC_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Curios.MODID, "main"))
            .networkProtocolVersion(() -> PTC_VERSION)
            .clientAcceptedVersions(PTC_VERSION::equals)
            .serverAcceptedVersions(PTC_VERSION::equals)
            .simpleChannel();

    private static int id = 0;

    public static void register() {
        registerMessage(CPacketOpenCurios.class, CPacketOpenCurios::encode, CPacketOpenCurios::decode, CPacketOpenCurios::handle);
        registerMessage(CPacketOpenVanilla.class, CPacketOpenVanilla::encode, CPacketOpenVanilla::decode, CPacketOpenVanilla::handle);
        registerMessage(CPacketScrollCurios.class, CPacketScrollCurios::encode, CPacketScrollCurios::decode, CPacketScrollCurios::handle);
        registerMessage(SPacketSyncCurios.class, SPacketSyncCurios::encode, SPacketSyncCurios::decode, SPacketSyncCurios::handle);
        registerMessage(SPacketScrollCurios.class, SPacketScrollCurios::encode, SPacketScrollCurios::decode, SPacketScrollCurios::handle);
        registerMessage(SPacketPlayEquip.class, SPacketPlayEquip::encode, SPacketPlayEquip::decode, SPacketPlayEquip::handle);
    }

    private static <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder,
                                              Function<PacketBuffer, MSG> decoder,
                                              BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        INSTANCE.registerMessage(id++, messageType, encoder, decoder, messageConsumer);
    }
}
