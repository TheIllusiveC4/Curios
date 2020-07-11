package top.theillusivec4.curios.common.network;

import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import top.theillusivec4.curios.api.CuriosApi;

public class NetworkPackets {

  public static final Identifier OPEN_CURIOS = new Identifier(CuriosApi.MODID, "open_curios");
  public static final Identifier SCROLL = new Identifier(CuriosApi.MODID, "scroll");
  public static final Identifier OPEN_VANILLA = new Identifier(CuriosApi.MODID, "open_vanilla");

  public static void init() {
    ServerSidePacketRegistry.INSTANCE.register(OPEN_CURIOS, ((packetContext, packetByteBuf) -> {
      packetContext.getTaskQueue().execute(() -> {
        PlayerEntity playerEntity = packetContext.getPlayer();

        if (playerEntity != null) {
          ItemStack stack = playerEntity.inventory.getCursorStack();
          playerEntity.inventory.setCursorStack(ItemStack.EMPTY);

        }
      });
    }));
  }
}
