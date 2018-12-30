package c4.curios.common.network.server;

import c4.curios.common.inventory.ContainerCurios;
import c4.curios.common.network.client.CPacketScrollCurios;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SPacketScrollCurios implements IMessage {

    int windowId;
    int index;

    public SPacketScrollCurios() {}

    public SPacketScrollCurios(int windowId, int index) {
        this.windowId = windowId;
        this.index = index;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        index = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        buf.writeInt(index);
    }

    public static class MessageHandler implements IMessageHandler<SPacketScrollCurios, IMessage> {

        @Override
        public IMessage onMessage(SPacketScrollCurios message, MessageContext ctx) {
            IThreadListener mainThread = Minecraft.getMinecraft();
            mainThread.addScheduledTask(() -> {
                EntityPlayerSP sp = Minecraft.getMinecraft().player;
                Container container = sp.openContainer;

                if (container instanceof ContainerCurios && container.windowId == message.windowId) {
                    ((ContainerCurios)container).scrollToIndex(message.index);
                }
            });
            return null;
        }
    }
}
