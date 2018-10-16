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

public class CPacketOpenCurios implements IMessage {

    public CPacketOpenCurios() {

    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<CPacketOpenCurios, IMessage> {

        @Override
        public IMessage onMessage(CPacketOpenCurios message, MessageContext ctx) {
            IThreadListener mainThread = ctx.getServerHandler().player.getServerWorld();
            mainThread.addScheduledTask(() -> {
                EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
                BlockPos pos = serverPlayer.getPosition();
                serverPlayer.openGui(Curios.MODID, GuiHandler.GUI_CURIO_ID, serverPlayer.world, pos.getX(),
                        pos.getY(), pos.getZ());
            });
            return null;
        }
    }
}
