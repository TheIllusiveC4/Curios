package top.theillusivec4.curios.api.internal.services;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.ApiStatus;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.data.IEntitiesData;
import top.theillusivec4.curios.api.type.data.ISlotData;

@ApiStatus.Internal
public interface ICuriosCodecs {

  Codec<ISlotType> slotTypeCodec();

  Codec<ISlotData.Entry> slotDataEntryCodec();

  Codec<IEntitiesData.Entry> entitiesDataEntryCodec();

  Codec<Holder<Attribute>> slotAttributeCodec();

  StreamCodec<RegistryFriendlyByteBuf, Holder<Attribute>> slotAttributeStreamCodec();

  StreamCodec<RegistryFriendlyByteBuf, ISlotType> slotTypeStreamCodec();
}
