package top.theillusivec4.curios.common.network.server.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosAPI;

import java.util.function.Supplier;

public class SPacketSyncContents {

    private int entityId;
    private int slotId;
    private String curioId;
    private ItemStack stack;

    public SPacketSyncContents(int entityId, String curioId, int slotId, ItemStack stack) {
        this.entityId = entityId;
        this.slotId = slotId;
        this.stack = stack.copy();
        this.curioId = curioId;
    }

    public static void encode(SPacketSyncContents msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeString(msg.curioId);
        buf.writeInt(msg.slotId);
        buf.writeItemStack(msg.stack);
    }

    public static SPacketSyncContents decode(PacketBuffer buf) {
        return new SPacketSyncContents(buf.readInt(), buf.readString(25), buf.readInt(), buf.readItemStack());
    }

    public static void handle(SPacketSyncContents msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().world.getEntityByID(msg.entityId);

            if (entity instanceof EntityLivingBase) {
                CuriosAPI.getCuriosHandler((EntityLivingBase)entity).ifPresent(handler -> handler.setStackInSlot(msg.curioId, msg.slotId, msg.stack));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
