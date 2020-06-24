package top.theillusivec4.curios.common.network.server.sync;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import top.theillusivec4.curios.api.CuriosApi;

public class SPacketSyncRender {

  private int entityId;
  private int slotId;
  private String curioId;
  private boolean value;

  public SPacketSyncRender(int entityId, String curioId, int slotId, boolean value) {
    this.entityId = entityId;
    this.slotId = slotId;
    this.curioId = curioId;
    this.value = value;
  }

  public static void encode(SPacketSyncRender msg, PacketBuffer buf) {
    buf.writeInt(msg.entityId);
    buf.writeString(msg.curioId);
    buf.writeInt(msg.slotId);
    buf.writeBoolean(msg.value);
  }

  public static SPacketSyncRender decode(PacketBuffer buf) {
    return new SPacketSyncRender(buf.readInt(), buf.readString(25), buf.readInt(),
        buf.readBoolean());
  }

  public static void handle(SPacketSyncRender msg, Supplier<Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ClientWorld world = Minecraft.getInstance().world;

      if (world != null) {
        Entity entity = world.getEntityByID(msg.entityId);

        if (entity instanceof LivingEntity) {
          CuriosApi.getCuriosHelper().getCuriosHandler((LivingEntity) entity).ifPresent(
              handler -> handler.getStacksHandler(msg.curioId).ifPresent(stacksHandler -> {
                int index = msg.slotId;
                NonNullList<Boolean> renderStatuses = stacksHandler.getRenders();

                if (renderStatuses.size() > index) {
                  renderStatuses.set(index, msg.value);
                }
              }));
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
