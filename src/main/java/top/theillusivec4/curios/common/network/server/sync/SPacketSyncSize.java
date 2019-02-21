package top.theillusivec4.curios.common.network.server.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosHelper;

import java.util.function.Supplier;

public class SPacketSyncSize {

    private int entityId;
    private String curioId;
    private int amount;

    public SPacketSyncSize(int entityId, String curioId, int amount) {
        this.entityId = entityId;
        this.curioId = curioId;
        this.amount = amount;
    }

    public static void encode(SPacketSyncSize msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeString(msg.curioId);
        buf.writeInt(msg.amount);
    }

    public static SPacketSyncSize decode(PacketBuffer buf) {
        return new SPacketSyncSize(buf.readInt(), buf.readString(25), buf.readInt());
    }

    public static void handle(SPacketSyncSize msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().world.getEntityByID(msg.entityId);

            if (entity instanceof EntityLivingBase) {
                CuriosHelper.getCuriosHandler((EntityLivingBase) entity).ifPresent(handler -> handler.addCurioSlot(msg.curioId, msg.amount));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
