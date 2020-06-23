package top.theillusivec4.curios.common.network.server;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

public class SPacketIcons {

  private int entrySize;
  private Map<String, ResourceLocation> map;

  public SPacketIcons(Map<String, ResourceLocation> map) {
    this.entrySize = map.size();
    this.map = map;
  }

  public static void encode(SPacketIcons msg, PacketBuffer buf) {
    buf.writeInt(msg.entrySize);

    for (Map.Entry<String, ResourceLocation> entry : msg.map.entrySet()) {
      buf.writeString(entry.getKey());
      buf.writeString(entry.getValue().toString());
    }
  }

  public static SPacketIcons decode(PacketBuffer buf) {
    int entrySize = buf.readInt();
    Map<String, ResourceLocation> map = new HashMap<>();

    for (int i = 0; i < entrySize; i++) {
      String key = buf.readString(100);
      map.put(buf.readString(25), new ResourceLocation(buf.readString(100)));
    }
    return new SPacketIcons(map);
  }

  public static void handle(SPacketIcons msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ClientWorld world = Minecraft.getInstance().world;

      if (world != null) {
        CuriosApi.getClientManager().clearIcons();

        for (Map.Entry<String, ResourceLocation> entry : msg.map.entrySet()) {
          CuriosApi.getClientManager().addIcon(entry.getKey(), entry.getValue());
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
