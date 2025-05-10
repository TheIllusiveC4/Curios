package top.theillusivec4.curios.api.common;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

/**
 * Determines the behavior when dropping items from curio slots as loot.
 */
public enum DropRule implements StringRepresentable {
  DEFAULT,
  ALWAYS_DROP,
  ALWAYS_KEEP,
  DESTROY;

  public static final StringRepresentable.EnumCodec<DropRule> CODEC =
      StringRepresentable.fromEnum(DropRule::values);
  public static final StreamCodec<ByteBuf, DropRule> STREAM_CODEC = ByteBufCodecs.idMapper(
      ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO), Enum::ordinal);

  @Nonnull
  @Override
  public String getSerializedName() {
    return this.name().toLowerCase();
  }
}
