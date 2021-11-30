package top.theillusivec4.curios.common.network.server.sync;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

public class SPacketSyncModifiers {

  private int entityId;
  private int entrySize;
  private Map<String, CompoundNBT> updates;

  public SPacketSyncModifiers(int entityId, Set<ICurioStacksHandler> updates) {
    Map<String, CompoundNBT> result = new LinkedHashMap<>();

    for (ICurioStacksHandler stacksHandler : updates) {
      result.put(stacksHandler.getIdentifier(), stacksHandler.getSyncTag());
    }
    this.entityId = entityId;
    this.entrySize = result.size();
    this.updates = result;
  }

  public SPacketSyncModifiers(Map<String, CompoundNBT> map, int entityId) {
    this.entityId = entityId;
    this.entrySize = map.size();
    this.updates = map;
  }

  public static void encode(SPacketSyncModifiers msg, PacketBuffer buf) {
    buf.writeInt(msg.entityId);
    buf.writeInt(msg.entrySize);

    for (Map.Entry<String, CompoundNBT> entry : msg.updates.entrySet()) {
      buf.writeString(entry.getKey());
      buf.writeCompoundTag(entry.getValue());
    }
  }

  public static SPacketSyncModifiers decode(PacketBuffer buf) {
    int entityId = buf.readInt();
    int entrySize = buf.readInt();
    Map<String, CompoundNBT> map = new LinkedHashMap<>();

    for (int i = 0; i < entrySize; i++) {
      String key = buf.readString(25);
      map.put(key, buf.readCompoundTag());
    }
    return new SPacketSyncModifiers(map, entityId);
  }

  public static void handle(SPacketSyncModifiers msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ClientWorld world = Minecraft.getInstance().world;

      if (world != null) {
        Entity entity = world.getEntityByID(msg.entityId);

        if (entity instanceof LivingEntity) {
          CuriosApi.getCuriosHelper().getCuriosHandler((LivingEntity) entity)
              .ifPresent(handler -> {
                Map<String, ICurioStacksHandler> curios = handler.getCurios();

                for (Map.Entry<String, CompoundNBT> entry : msg.updates.entrySet()) {
                  String id = entry.getKey();
                  ICurioStacksHandler stacksHandler = curios.get(id);

                  if (stacksHandler != null) {
                    stacksHandler.applySyncTag(entry.getValue());
                  }
                }

                if (entity instanceof PlayerEntity) {
                  PlayerEntity player = (PlayerEntity) entity;

                  if (player.openContainer instanceof CuriosContainer) {
                    ((CuriosContainer) player.openContainer).resetSlots();
                  }
                }
              });
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
