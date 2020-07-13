package top.theillusivec4.curios.common.network;

import io.netty.buffer.Unpooled;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.component.ICurio;
import top.theillusivec4.curios.client.screen.CuriosScreen;
import top.theillusivec4.curios.common.inventory.screen.CuriosScreenHandler;
import top.theillusivec4.curios.common.inventory.screen.CuriosScreenHandlerFactory;

public class NetworkPackets {

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
            .sendToPlayer((PlayerEntity) livingEntity, BREAK, packetByteBuf);
      }
      PlayerStream.watching(livingEntity).forEach(
          watcher -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(watcher, BREAK, packetByteBuf));
    });

    ClientSidePacketRegistry.INSTANCE.register(BREAK, (((packetContext, packetByteBuf) -> {
      int entityId = packetByteBuf.readInt();
      String curioId = packetByteBuf.readString(25);
      int index = packetByteBuf.readInt();
      packetContext.getTaskQueue().execute(() -> {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client != null && client.world != null) {
          Entity entity = client.world.getEntityById(entityId);

          if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {
              ItemStack stack = handler.getStacksHandler(curioId)
                  .map(stacksHandler -> stacksHandler.getStacks().getStack(index))
                  .orElse(ItemStack.EMPTY);
              Optional<ICurio> maybeCurio = CuriosApi.getCuriosHelper().getCurio(stack);
              maybeCurio.ifPresent(curio -> curio.curioBreak(stack, livingEntity));

              if (!maybeCurio.isPresent()) {
                ICurio.playBreakAnimation(stack, livingEntity);
              }
            });
          }
        }
      });
    })));

    ClientSidePacketRegistry.INSTANCE.register(SET_ICONS, (((packetContext, packetByteBuf) -> {
      int entrySize = packetByteBuf.readInt();
      Map<String, Identifier> map = new HashMap<>();

      for (int i = 0; i < entrySize; i++) {
        map.put(packetByteBuf.readString(25), new Identifier(packetByteBuf.readString(100)));
      }
      packetContext.getTaskQueue().execute(() -> {
        CuriosApi.getIconHelper().clearIcons();

        for (Map.Entry<String, Identifier> entry : map.entrySet()) {
          CuriosApi.getIconHelper().addIcon(entry.getKey(), entry.getValue());
        }
      });
    })));

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

    ClientSidePacketRegistry.INSTANCE.register(GRAB_ITEM, (((packetContext, packetByteBuf) -> {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity clientPlayerEntity = client.player;

      if (clientPlayerEntity != null) {
        clientPlayerEntity.inventory.setCursorStack(packetByteBuf.readItemStack());
      }
    })));

    ClientSidePacketRegistry.INSTANCE.register(SCROLL, (((packetContext, packetByteBuf) -> {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity clientPlayerEntity = client.player;
      Screen screen = client.currentScreen;
      int syncId = packetByteBuf.readInt();
      int scrollIndex = packetByteBuf.readInt();

      if (clientPlayerEntity != null) {
        ScreenHandler screenHandler = clientPlayerEntity.currentScreenHandler;

        if (screenHandler instanceof CuriosScreenHandler && screenHandler.syncId == syncId) {
          ((CuriosScreenHandler) screenHandler).scrollToIndex(scrollIndex);
        }
      }

      if (screen instanceof CuriosScreen) {
        ((CuriosScreen) screen).updateRenderButtons();
      }
    })));

    ServerSidePacketRegistry.INSTANCE.register(SCROLL,
        (((packetContext, packetByteBuf) -> packetContext.getTaskQueue().execute(() -> {
          PlayerEntity playerEntity = packetContext.getPlayer();
          ScreenHandler screenHandler = playerEntity.currentScreenHandler;
          int syncId = packetByteBuf.readInt();
          int lastScrollIndex = packetByteBuf.readInt();

          if (screenHandler instanceof CuriosScreenHandler && screenHandler.syncId == syncId) {
            ((CuriosScreenHandler) screenHandler).scrollToIndex(lastScrollIndex);
          }
        }))));

    ServerSidePacketRegistry.INSTANCE.register(TOGGLE_RENDER, (((packetContext, packetByteBuf) -> {
      int index = packetByteBuf.readInt();
      String id = packetByteBuf.readString();
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
