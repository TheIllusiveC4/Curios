package top.theillusivec4.curios.common.network.server;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SPacketGrabbedItem {

  private ItemStack stack;

  public SPacketGrabbedItem(ItemStack stackIn) {
    this.stack = stackIn;
  }

  public static void encode(SPacketGrabbedItem msg, PacketBuffer buf) {
    buf.writeItemStack(msg.stack);
  }

  public static SPacketGrabbedItem decode(PacketBuffer buf) {
    return new SPacketGrabbedItem(buf.readItemStack());
  }

  public static void handle(SPacketGrabbedItem msg, Supplier<Context> ctx) {

    ctx.get().enqueueWork(() -> {
      ClientPlayerEntity sp = Minecraft.getInstance().player;
      sp.inventory.setItemStack(msg.stack);
    });
    ctx.get().setPacketHandled(true);
  }
}
