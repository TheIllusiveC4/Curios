package c4.curios.common.network;

import c4.curios.Curios;
import c4.curios.common.network.client.CPacketOpenCurios;
import c4.curios.common.network.client.CPacketOpenVanilla;
import c4.curios.common.network.client.CPacketScrollCurios;
import c4.curios.common.network.server.SPacketEntityCurios;
import c4.curios.common.network.server.SPacketScrollCurios;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Curios.MODID);

    private static int id = 0;

    public static void init() {
        registerMessage(CPacketOpenCurios.MessageHandler.class, CPacketOpenCurios.class, Side.SERVER);
        registerMessage(CPacketOpenVanilla.MessageHandler.class, CPacketOpenVanilla.class, Side.SERVER);
        registerMessage(CPacketScrollCurios.MessageHandler.class, CPacketScrollCurios.class, Side.SERVER);
        registerMessage(SPacketEntityCurios.MessageHandler.class, SPacketEntityCurios.class, Side.CLIENT);
        registerMessage(SPacketScrollCurios.MessageHandler.class, SPacketScrollCurios.class, Side.CLIENT);
    }

    @SuppressWarnings("unchecked")
    private static void registerMessage(Class messageHandler, Class messageRequest, Side side) {
        INSTANCE.registerMessage(messageHandler, messageRequest, id++, side);
    }
}
