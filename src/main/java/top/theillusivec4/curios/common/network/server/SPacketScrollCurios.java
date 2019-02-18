package top.theillusivec4.curios.common.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.common.inventory.ContainerCurios;

import java.util.function.Supplier;

public class SPacketScrollCurios {

    int windowId;
    int index;

    public SPacketScrollCurios(int windowId, int index) {
        this.windowId = windowId;
        this.index = index;
    }

    public static void encode(SPacketScrollCurios msg, PacketBuffer buf) {
        buf.writeInt(msg.windowId);
        buf.writeInt(msg.index);
    }

    public static SPacketScrollCurios decode(PacketBuffer buf) {
        return new SPacketScrollCurios(buf.readInt(), buf.readInt());
    }

    public static void handle(SPacketScrollCurios msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            EntityPlayerSP sp = Minecraft.getInstance().player;
            Container container = sp.openContainer;

            if (container instanceof ContainerCurios && container.windowId == msg.windowId) {
                ((ContainerCurios)container).scrollToIndex(msg.index);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
