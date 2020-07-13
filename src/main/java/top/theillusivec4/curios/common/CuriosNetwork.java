package top.theillusivec4.curios.common;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.inventory.screen.CuriosScreenHandler;
import top.theillusivec4.curios.common.inventory.screen.CuriosScreenHandlerFactory;

public class CuriosNetwork {

  public static final Identifier OPEN_CURIOS = new Identifier(CuriosApi.MODID, "open_curios");
  public static final Identifier SCROLL = new Identifier(CuriosApi.MODID, "scroll");
  public static final Identifier OPEN_VANILLA = new Identifier(CuriosApi.MODID, "open_vanilla");
  public static final Identifier TOGGLE_RENDER = new Identifier(CuriosApi.MODID, "toggle_render");
  public static final Identifier GRAB_ITEM = new Identifier(CuriosApi.MODID, "grab_item");
  public static final Identifier SET_ICONS = new Identifier(CuriosApi.MODID, "set_icons");
  public static final Identifier BREAK = new Identifier(CuriosApi.MODID, "break");

  public static void registerPackets() {
    CuriosApi.getCuriosHelper().setBrokenCurioConsumer((id, index, livingEntity) -> {
      PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
      packetByteBuf.writeInt(livingEntity.getEntityId());
      packetByteBuf.writeString(id);
      packetByteBuf.writeInt(index);

      if (livingEntity instanceof PlayerEntity) {
        ServerSidePacketRegistry.INSTANCE
            .sendToPlayer((PlayerEntity) livingEntity, CuriosNetwork.BREAK, packetByteBuf);
      }
      PlayerStream.watching(livingEntity).forEach(watcher -> ServerSidePacketRegistry.INSTANCE
          .sendToPlayer(watcher, CuriosNetwork.BREAK, packetByteBuf));
    });

    ServerSidePacketRegistry.INSTANCE.register(OPEN_CURIOS,
        ((packetContext, packetByteBuf) -> packetContext.getTaskQueue().execute(() -> {
          PlayerEntity playerEntity = packetContext.getPlayer();

          if (playerEntity != null) {
            ItemStack stack = playerEntity.inventory.getCursorStack();
            playerEntity.inventory.setCursorStack(ItemStack.EMPTY);
            playerEntity.openHandledScreen(new CuriosScreenHandlerFactory());

            if (!stack.isEmpty()) {
              playerEntity.inventory.setCursorStack(stack);
              PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
              buf.writeItemStack(stack);
              ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerEntity, GRAB_ITEM, buf);
            }
          }
        })));

    ServerSidePacketRegistry.INSTANCE.register(SCROLL, (((packetContext, packetByteBuf) -> {
      int syncId = packetByteBuf.readInt();
      int lastScrollIndex = packetByteBuf.readInt();

      packetContext.getTaskQueue().execute(() -> {
        PlayerEntity playerEntity = packetContext.getPlayer();
        ScreenHandler screenHandler = playerEntity.currentScreenHandler;

        if (screenHandler instanceof CuriosScreenHandler && screenHandler.syncId == syncId) {
          ((CuriosScreenHandler) screenHandler).scrollToIndex(lastScrollIndex);
        }
      });
    })));

    ServerSidePacketRegistry.INSTANCE.register(TOGGLE_RENDER, (((packetContext, packetByteBuf) -> {
      int index = packetByteBuf.readInt();
      String id = packetByteBuf.readString(25);

      packetContext.getTaskQueue().execute(() -> {
        PlayerEntity playerEntity = packetContext.getPlayer();
        CuriosApi.getCuriosHelper().getCuriosHandler(playerEntity)
            .ifPresent(handler -> handler.getStacksHandler(id).ifPresent(stacksHandler -> {
              DefaultedList<Boolean> renderStatuses = stacksHandler.getRenders();

              if (renderStatuses.size() > index) {
                boolean value = !renderStatuses.get(index);
                renderStatuses.set(index, value);
                handler.sync();
              }
            }));
      });
    })));
  }
}
