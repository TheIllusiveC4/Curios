package c4.curios.common.network;

import c4.curios.Curios;
import c4.curios.client.GuiHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CPacketOpenVanilla implements IMessage {

    public CPacketOpenVanilla() {

    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<CPacketOpenVanilla, IMessage> {

        @Override
        public IMessage onMessage(CPacketOpenVanilla message, MessageContext ctx) {
            IThreadListener mainThread = ctx.getServerHandler().player.getServerWorld();
            mainThread.addScheduledTask(() -> {
                EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
                serverPlayer.openContainer = serverPlayer.inventoryContainer;
            });
            return null;
        }
    }
}
