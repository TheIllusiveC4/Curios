package top.theillusivec4.curios.common.network.client;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import top.theillusivec4.curios.client.CurioContainerHandler;

import java.util.function.Supplier;

public class CPacketOpenCurios {

    public static void encode(CPacketOpenCurios msg, PacketBuffer buf) {}

    public static CPacketOpenCurios decode(PacketBuffer buf) {
        return new CPacketOpenCurios();
    }

    public static void handle(CPacketOpenCurios msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            EntityPlayerMP sender = ctx.get().getSender();

            if (sender != null) {
                sender.closeContainer();
                NetworkHooks.openGui(sender, new CurioContainerHandler(), null);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
