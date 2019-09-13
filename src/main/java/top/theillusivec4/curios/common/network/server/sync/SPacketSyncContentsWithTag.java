package top.theillusivec4.curios.common.network.server.sync;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosAPI;

public class SPacketSyncContentsWithTag {

  private int entityId;
  private int slotId;
  private String curioId;
  private ItemStack stack;
  private CompoundNBT compound;

  public SPacketSyncContentsWithTag(int entityId, String curioId, int slotId, ItemStack stack,
      CompoundNBT compound) {

    this.entityId = entityId;
    this.slotId = slotId;
    this.stack = stack.copy();
    this.curioId = curioId;
    this.compound = compound;
  }

  public static void encode(SPacketSyncContentsWithTag msg, PacketBuffer buf) {

    buf.writeInt(msg.entityId);
    buf.writeString(msg.curioId);
    buf.writeInt(msg.slotId);
    buf.writeItemStack(msg.stack);
    buf.writeCompoundTag(msg.compound);
  }

  public static SPacketSyncContentsWithTag decode(PacketBuffer buf) {

    return new SPacketSyncContentsWithTag(buf.readInt(), buf.readString(25), buf.readInt(),
        buf.readItemStack(), buf.readCompoundTag());
  }

  public static void handle(SPacketSyncContentsWithTag msg, Supplier<NetworkEvent.Context> ctx) {

    ctx.get().enqueueWork(() -> {
      Entity entity = Minecraft.getInstance().world.getEntityByID(msg.entityId);

      if (entity instanceof LivingEntity) {
        CuriosAPI.getCuriosHandler((LivingEntity) entity).ifPresent(handler -> {
          ItemStack stack = msg.stack;
          CuriosAPI.getCurio(stack).ifPresent(curio -> curio.readSyncTag(msg.compound));
          handler.setStackInSlot(msg.curioId, msg.slotId, stack);
        });
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
