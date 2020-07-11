package top.theillusivec4.curios.common.inventory;

import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.collection.DefaultedList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.component.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.CuriosRegistry;
import top.theillusivec4.curios.common.network.NetworkPackets;
import top.theillusivec4.curios.mixin.IScreenHandlerAccessor;

public class CuriosScreenHandler extends PlayerScreenHandler {

  private final PlayerEntity owner;
  private final ICuriosItemHandler curiosHandler;

  private int lastScrollIndex;
  private boolean cosmeticColumn;

  public CuriosScreenHandler(int syncId, PlayerInventory playerInventory) {
    super(playerInventory, playerInventory.player.world.isClient, playerInventory.player);
    this.owner = playerInventory.player;
    this.curiosHandler = CuriosApi.getCuriosHelper().getCuriosHandler(playerInventory.player)
        .orElse(null);
    this.getCuriosHandler().ifPresent(curios -> {
      Map<String, ICurioStacksHandler> curioMap = curios.getCurios();
      int slots = 0;
      int yOffset = 12;

      for (String identifier : curioMap.keySet()) {
        ICurioStacksHandler stacksHandler = curioMap.get(identifier);
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        if (stacksHandler.isVisible()) {

          for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {
            this.addSlot(new CurioSlot(owner, stackHandler, i, identifier, -18, yOffset,
                stacksHandler.getRenders()));

            if (stacksHandler.hasCosmetic()) {
              IDynamicStackHandler cosmeticHandler = stacksHandler.getCosmeticStacks();
              this.cosmeticColumn = true;
              this.addSlot(
                  new CosmeticCurioSlot(owner, cosmeticHandler, i, identifier, -37, yOffset));
            }
            yOffset += 18;
            slots++;
          }
        }
      }
    });
  }

  public boolean hasCosmeticColumn() {
    return this.cosmeticColumn;
  }

  public Optional<ICuriosItemHandler> getCuriosHandler() {
    return Optional.of(curiosHandler);
  }

  public void scrollToIndex(int indexIn) {
    this.getCuriosHandler().ifPresent(curios -> {
      Map<String, ICurioStacksHandler> curioMap = curios.getCurios();
      int slots = 0;
      int yOffset = 12;
      int index = 0;
      this.slots.subList(46, this.slots.size()).clear();
      DefaultedList<ItemStack> stacks = ((IScreenHandlerAccessor) this).getStacks();

      if (stacks != null) {
        stacks.subList(46, stacks.size()).clear();
      }

      for (String identifier : curioMap.keySet()) {
        ICurioStacksHandler stacksHandler = curioMap.get(identifier);
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        if (stacksHandler.isVisible()) {

          for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {

            if (index >= indexIn) {
              this.addSlot(new CurioSlot(owner, stackHandler, i, identifier, -18, yOffset,
                  stacksHandler.getRenders()));

              if (stacksHandler.hasCosmetic()) {
                IDynamicStackHandler cosmeticHandler = stacksHandler.getCosmeticStacks();
                this.cosmeticColumn = true;
                this.addSlot(
                    new CosmeticCurioSlot(owner, cosmeticHandler, i, identifier, -37, yOffset));
              }
              yOffset += 18;
              slots++;
            }
            index++;
          }
        }
      }

      if (this.onServer) {
        PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
        packetByteBuf.writeInt(this.syncId);
        packetByteBuf.writeInt(indexIn);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(owner, NetworkPackets.SCROLL, packetByteBuf);
      }
      lastScrollIndex = indexIn;
    });
  }

  public void scrollToPosition(float pos) {
    this.getCuriosHandler().ifPresent(curios -> {
      int k = (curios.getSlots() - 8);
      int j = (int) ((double) (pos * (float) k) + 0.5D);

      if (j < 0) {
        j = 0;
      }

      if (j == this.lastScrollIndex) {
        return;
      }

      if (!this.onServer) {
        PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
        packetByteBuf.writeInt(this.syncId);
        packetByteBuf.writeInt(this.lastScrollIndex);
        ClientSidePacketRegistry.INSTANCE.sendToServer(NetworkPackets.SCROLL, packetByteBuf);
      }
    });
  }

  public boolean canScroll() {
    return this.getCuriosHandler().map(curios -> curios.getSlots() > 8).orElse(false);
  }

  @Override
  public ScreenHandlerType<?> getType() {
    return CuriosRegistry.CURIOS_SCREEN;
  }
}
