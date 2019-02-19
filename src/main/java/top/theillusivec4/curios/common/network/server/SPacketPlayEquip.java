package top.theillusivec4.curios.common.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosAPI;

import java.util.function.Supplier;

public class SPacketPlayEquip {

    final ItemStack stack;

    public SPacketPlayEquip(ItemStack stack) {
        this.stack = stack;
    }

    public static void encode(SPacketPlayEquip msg, PacketBuffer buf) {
        buf.writeItemStack(msg.stack);
    }

    public static SPacketPlayEquip decode(PacketBuffer buf) {
        return new SPacketPlayEquip(buf.readItemStack());
    }

    public static void handle(SPacketPlayEquip msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            EntityPlayerSP sp = Minecraft.getInstance().player;
            CuriosAPI.getCurio(msg.stack).ifPresent(curio -> curio.playEquipSound(msg.stack, sp));
        });
        ctx.get().setPacketHandled(true);
    }
}
