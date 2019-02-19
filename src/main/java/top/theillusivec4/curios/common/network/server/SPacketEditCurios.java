package top.theillusivec4.curios.common.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosAPI;

import java.util.function.Supplier;

public class SPacketEditCurios {

    private int entityId;
    private String curioId;
    private boolean remove;

    public SPacketEditCurios(int entityId, String curioId, boolean remove) {
        this.entityId = entityId;
        this.curioId = curioId;
        this.remove = remove;
    }

    public static void encode(SPacketEditCurios msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeString(msg.curioId);
        buf.writeBoolean(msg.remove);
    }

    public static SPacketEditCurios decode(PacketBuffer buf) {
        return new SPacketEditCurios(buf.readInt(), buf.readString(25), buf.readBoolean());
    }

    public static void handle(SPacketEditCurios msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().world.getEntityByID(msg.entityId);

            if (entity instanceof EntityLivingBase) {
                CuriosAPI.getCuriosHandler((EntityLivingBase) entity).ifPresent(handler -> {

                    if (msg.remove) {
                        handler.disableCurio(msg.curioId);
                    } else {
                        handler.enableCurio(msg.curioId);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
