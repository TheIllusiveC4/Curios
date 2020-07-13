package top.theillusivec4.curios.mixin;

import io.netty.buffer.Unpooled;
import java.util.Collection;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.common.CuriosNetwork;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

  @Inject(method = "onPlayerConnect", at = @At("TAIL"))
  public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player,
      CallbackInfo cb) {
    Collection<ISlotType> slotTypes = CuriosApi.getSlotHelper().getSlotTypes();
    PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
    packetByteBuf.writeInt(slotTypes.size());
    slotTypes.forEach(type -> {
      packetByteBuf.writeString(type.getIdentifier());
      packetByteBuf.writeString(type.getIcon().toString());
    });
    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, CuriosNetwork.SET_ICONS, packetByteBuf);
  }
}
