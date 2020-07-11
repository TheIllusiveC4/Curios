package top.theillusivec4.curios.client;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.network.NetworkPackets;

public class CuriosClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    CuriosApi.setIconHelper(new IconHelper());
    KeyRegistry.registerKeys();
    ClientTickCallback.EVENT.register(client -> {

      if (KeyRegistry.openCurios.wasPressed()) {
        ClientSidePacketRegistry.INSTANCE
            .sendToServer(NetworkPackets.OPEN_CURIOS, new PacketByteBuf(Unpooled.buffer()));
      }
    });
  }
}
