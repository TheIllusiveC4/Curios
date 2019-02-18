package top.theillusivec4.curios.common.network.client;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.common.inventory.ContainerCurios;

import java.util.function.Supplier;

public class CPacketScrollCurios {

    int windowId;
    int index;

    public CPacketScrollCurios() {}

    public CPacketScrollCurios(int windowId, int index) {
        this.windowId = windowId;
        this.index = index;
    }

    public static void encode(CPacketScrollCurios msg, PacketBuffer buf) {
        buf.writeInt(msg.windowId);
        buf.writeInt(msg.index);
    }

    public static CPacketScrollCurios decode(PacketBuffer buf) {
        return new CPacketScrollCurios(buf.readInt(), buf.readInt());
    }

    public static void handle(CPacketScrollCurios msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            EntityPlayerMP sender = ctx.get().getSender();

            if (sender != null) {
                Container container = sender.openContainer;

                if (container instanceof ContainerCurios && container.windowId == msg.windowId) {
                    ((ContainerCurios)container).scrollToIndex(msg.index);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
