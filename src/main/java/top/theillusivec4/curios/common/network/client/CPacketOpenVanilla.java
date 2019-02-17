package top.theillusivec4.curios.common.network.client;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CPacketOpenVanilla {

    public static void encode(CPacketOpenVanilla msg, PacketBuffer buf) {}

    public static CPacketOpenVanilla decode(PacketBuffer buf) {
        return new CPacketOpenVanilla();
    }

    public static void handle(CPacketOpenVanilla msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            EntityPlayerMP sender = ctx.get().getSender();

            if (sender != null) {
                sender.closeContainer();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
