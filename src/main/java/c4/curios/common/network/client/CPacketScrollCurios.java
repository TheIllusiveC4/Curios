package c4.curios.common.network.client;

import c4.curios.common.inventory.ContainerCurios;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CPacketScrollCurios implements IMessage {

    int windowId;
    float pos;

    public CPacketScrollCurios() {}

    public CPacketScrollCurios(int windowId, float pos) {
        this.windowId = windowId;
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        pos = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        buf.writeFloat(pos);
    }

    public static class MessageHandler implements IMessageHandler<CPacketScrollCurios, IMessage> {

        @Override
        public IMessage onMessage(CPacketScrollCurios message, MessageContext ctx) {
            IThreadListener mainThread = ctx.getServerHandler().player.getServerWorld();
            mainThread.addScheduledTask(() -> {
                EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
                Container container = serverPlayer.openContainer;
                if (container instanceof ContainerCurios && container.windowId == message.windowId) {
                    ((ContainerCurios)container).scrollTo(message.pos);
                }
            });
            return null;
        }
    }
}
