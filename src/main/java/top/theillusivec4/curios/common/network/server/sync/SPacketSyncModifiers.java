package top.theillusivec4.curios.common.network.server.sync;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.SlotModifiersUpdatedEvent;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

public class SPacketSyncModifiers {

  private int entityId;
  private int entrySize;
  private Map<String, CompoundTag> updates;

  public SPacketSyncModifiers(int entityId, Set<ICurioStacksHandler> updates) {
    Map<String, CompoundTag> result = new LinkedHashMap<>();

    for (ICurioStacksHandler stacksHandler : updates) {
      result.put(stacksHandler.getIdentifier(), stacksHandler.getSyncTag());
    }
    this.entityId = entityId;
    this.entrySize = result.size();
    this.updates = result;
  }

  public SPacketSyncModifiers(Map<String, CompoundTag> map, int entityId) {
    this.entityId = entityId;
    this.entrySize = map.size();
    this.updates = map;
  }

  public static void encode(SPacketSyncModifiers msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.entityId);
    buf.writeInt(msg.entrySize);

    for (Map.Entry<String, CompoundTag> entry : msg.updates.entrySet()) {
      buf.writeUtf(entry.getKey());
      buf.writeNbt(entry.getValue());
    }
  }

  public static SPacketSyncModifiers decode(FriendlyByteBuf buf) {
    int entityId = buf.readInt();
    int entrySize = buf.readInt();
    Map<String, CompoundTag> map = new LinkedHashMap<>();

    for (int i = 0; i < entrySize; i++) {
      String key = buf.readUtf(25);
      map.put(key, buf.readNbt());
    }
    return new SPacketSyncModifiers(map, entityId);
  }

  public static void handle(SPacketSyncModifiers msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ClientLevel world = Minecraft.getInstance().level;

      if (world != null) {
        Entity entity = world.getEntity(msg.entityId);

        if (entity instanceof LivingEntity livingEntity) {
          CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity)
              .ifPresent(handler -> {
                Map<String, ICurioStacksHandler> curios = handler.getCurios();

                for (Map.Entry<String, CompoundTag> entry : msg.updates.entrySet()) {
                  String id = entry.getKey();
                  ICurioStacksHandler stacksHandler = curios.get(id);

                  if (stacksHandler != null) {
                    stacksHandler.applySyncTag(entry.getValue());
                  }
                }

                if (!msg.updates.isEmpty()) {
                  MinecraftForge.EVENT_BUS.post(
                      new SlotModifiersUpdatedEvent(livingEntity, msg.updates.keySet()));
                }

                if (entity instanceof Player player) {

                  if (player.containerMenu instanceof CuriosContainer) {
                    ((CuriosContainer) player.containerMenu).resetSlots();
                  }
                }
              });
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
