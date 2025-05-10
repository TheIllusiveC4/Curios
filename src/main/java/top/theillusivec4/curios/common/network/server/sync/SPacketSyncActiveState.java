package top.theillusivec4.curios.common.network.server.sync;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.curios.CuriosConstants;

import javax.annotation.Nonnull;

public record SPacketSyncActiveState(int entityId, String curioId, int slotId, boolean value) implements
    CustomPacketPayload {

  public static final Type<SPacketSyncActiveState> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "sync_active"));

  public static final StreamCodec<RegistryFriendlyByteBuf, SPacketSyncActiveState> STREAM_CODEC =
      StreamCodec.composite(ByteBufCodecs.INT, SPacketSyncActiveState::entityId,
                            ByteBufCodecs.STRING_UTF8, SPacketSyncActiveState::curioId, ByteBufCodecs.INT,
                            SPacketSyncActiveState::slotId, ByteBufCodecs.BOOL, SPacketSyncActiveState::value,
                            SPacketSyncActiveState::new);

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
