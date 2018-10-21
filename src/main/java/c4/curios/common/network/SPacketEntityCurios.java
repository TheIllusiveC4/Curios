package c4.curios.common.network;

import c4.curios.Curios;
import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurioItemHandler;
import c4.curios.client.GuiHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SPacketEntityCurios implements IMessage {

    private int entityId;
    private int slotId;
    private ItemStack stack = ItemStack.EMPTY;

    public SPacketEntityCurios() {}

    public SPacketEntityCurios(int entityId, int slotId, ItemStack stack) {
        this.entityId = entityId;
        this.slotId = slotId;
        this.stack = stack.copy();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(slotId);
        ByteBufUtils.writeItemStack(buf, stack);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.slotId = buf.readInt();
        this.stack = ByteBufUtils.readItemStack(buf);
    }

    public static class MessageHandler implements IMessageHandler<SPacketEntityCurios, IMessage> {

        @Override
        public IMessage onMessage(SPacketEntityCurios message, MessageContext ctx) {
            IThreadListener mainThread = Minecraft.getMinecraft();
            mainThread.addScheduledTask(() -> {
                Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.entityId);

                if (entity instanceof EntityLivingBase) {
                    ICurioItemHandler handler = CuriosAPI.getCuriosHandler((EntityLivingBase)entity);

                    if (handler != null) {
                        handler.setStackInSlot(message.slotId, message.stack);
                    }
                }
            });
            return null;
        }
    }
}
