package top.theillusivec4.curios.common.network.client;

import java.util.function.Supplier;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncRender;

public class CPacketToggleRender {

  String id;
  int index;

  public CPacketToggleRender(String id, int index) {
    this.id = id;
    this.index = index;
  }

  public static void encode(CPacketToggleRender msg, PacketBuffer buf) {
    buf.writeString(msg.id);
    buf.writeInt(msg.index);
  }

  public static CPacketToggleRender decode(PacketBuffer buf) {
    return new CPacketToggleRender(buf.readString(100), buf.readInt());
  }

  public static void handle(CPacketToggleRender msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayerEntity sender = ctx.get().getSender();

      if (sender != null) {
        CuriosApi.getCuriosHelper().getCuriosHandler(sender)
            .ifPresent(handler -> handler.getStacksHandler(msg.id).ifPresent(stacksHandler -> {
              NonNullList<Boolean> renderStatuses = stacksHandler.getRenders();

              if (renderStatuses.size() > msg.index) {
                boolean value = !renderStatuses.get(msg.index);
                renderStatuses.set(msg.index, value);
                NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sender),
                    new SPacketSyncRender(sender.getEntityId(), msg.id, msg.index, value));
              }
            }));
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
