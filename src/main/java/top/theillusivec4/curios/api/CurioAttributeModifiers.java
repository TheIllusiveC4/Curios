/*
 * Copyright (c) 2018-2024 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package top.theillusivec4.curios.api;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.extensions.IHolderExtension;
import top.theillusivec4.curios.api.common.slot.SlotTypePredicate;
import top.theillusivec4.curios.api.type.ISlotType;

/**
 * A record used for data component representation of curio attribute modifiers.
 *
 * @param modifiers     The attribute modifiers.
 * @param showInTooltip Whether to show in tooltips.
 * @see net.minecraft.world.item.component.ItemAttributeModifiers
 */
public record CurioAttributeModifiers(List<Entry> modifiers, boolean showInTooltip) {

  private static final Codec<CurioAttributeModifiers> FULL_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
              Entry.CODEC.listOf().fieldOf("modifiers")
                  .forGetter(CurioAttributeModifiers::modifiers),
              Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.TRUE)
                  .forGetter(CurioAttributeModifiers::showInTooltip)
          )
          .apply(instance, CurioAttributeModifiers::new)
  );

  public static final CurioAttributeModifiers EMPTY = new CurioAttributeModifiers(List.of(), true);
  public static final Codec<CurioAttributeModifiers> CODEC =
      Codec.withAlternative(FULL_CODEC, Entry.CODEC.listOf(),
                            list -> new CurioAttributeModifiers(list, true));
  public static final StreamCodec<RegistryFriendlyByteBuf, CurioAttributeModifiers>
      STREAM_CODEC = StreamCodec.composite(
      Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
      CurioAttributeModifiers::modifiers,
      ByteBufCodecs.BOOL,
      CurioAttributeModifiers::showInTooltip,
      CurioAttributeModifiers::new
  );

  public static Builder builder() {
    return new Builder();
  }

  public CurioAttributeModifiers withTooltip(boolean showInTooltip) {
    return new CurioAttributeModifiers(this.modifiers, showInTooltip);
  }

  public CurioAttributeModifiers withModifierAdded(Holder<Attribute> attribute,
                                                   AttributeModifier attributeModifier,
                                                   ISlotType slotType) {
    return this.withModifierAdded(attribute, attributeModifier, slotType.getId());
  }

  public CurioAttributeModifiers withModifierAdded(Holder<Attribute> attribute,
                                                   AttributeModifier attributeModifier,
                                                   String slotId) {
    return this.withModifierAdded(attribute, attributeModifier,
                                  SlotTypePredicate.builder().withId(slotId).build());
  }

  public CurioAttributeModifiers withModifierAdded(Holder<Attribute> attribute,
                                                   AttributeModifier attributeModifier,
                                                   SlotTypePredicate slotTypePredicate) {
    ImmutableList.Builder<Entry> builder =
        ImmutableList.builderWithExpectedSize(this.modifiers.size() + 1);

    for (Entry entry : this.modifiers) {

      if (!entry.modifier.id().equals(attributeModifier.id())) {
        builder.add(entry);
      }
    }
    builder.add(new Entry(attribute, attributeModifier, slotTypePredicate));
    return new CurioAttributeModifiers(builder.build(), this.showInTooltip);
  }

  public void forEach(SlotContext slotContext,
                      BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {

    for (Entry entry : this.modifiers) {

      if (entry.slotType().matches(slotContext.identifier())) {
        AttributeModifier modifier = entry.modifier();
        consumer.accept(entry.attributeHolder(),
                        new AttributeModifier(
                            modifier.id().withSuffix("/" + slotContext.identifier()),
                            modifier.amount(),
                            modifier.operation()));
      }
    }
  }

  public void forEach(ISlotType slotType,
                      BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {

    for (Entry entry : this.modifiers) {

      if (entry.slotType().matches(slotType)) {
        AttributeModifier modifier = entry.modifier();
        consumer.accept(entry.attributeHolder(),
                        new AttributeModifier(
                            modifier.id().withSuffix("/" + slotType.getId()),
                            modifier.amount(),
                            modifier.operation()));
      }
    }
  }

  @Deprecated(forRemoval = true)
  public CurioAttributeModifiers withModifierAdded(ResourceLocation attribute,
                                                   AttributeModifier attributeModifier,
                                                   String slot) {
    ImmutableList.Builder<Entry> builder =
        ImmutableList.builderWithExpectedSize(this.modifiers.size() + 1);

    for (Entry entry : this.modifiers) {

      if (!entry.modifier.id().equals(attributeModifier.id())) {
        builder.add(entry);
      }
    }

    builder.add(new Entry(attribute, attributeModifier, slot));
    return new CurioAttributeModifiers(builder.build(), this.showInTooltip);
  }

  @Deprecated(forRemoval = true)
  public void forEach(String slot, BiConsumer<ResourceLocation, AttributeModifier> consumer) {

    for (Entry entry : this.modifiers) {

      if (entry.slotType().matches(slot)) {
        AttributeModifier modifier = entry.modifier();
        consumer.accept(entry.attribute(),
                        new AttributeModifier(
                            modifier.id().withSuffix("/" + slot),
                            modifier.amount(),
                            modifier.operation()));
      }
    }
  }

  public static class Builder {

    private final ImmutableList.Builder<Entry> entries = ImmutableList.builder();

    Builder() {
    }

    public Builder addSlotModifier(String slotId, AttributeModifier attributeModifier) {
      this.entries.add(
          new Entry(SlotAttribute.getOrCreate(slotId), attributeModifier, SlotTypePredicate.ANY));
      return this;
    }

    public Builder addSlotModifier(String slotId, AttributeModifier attributeModifier,
                                   String... slot) {
      this.entries.add(new Entry(SlotAttribute.getOrCreate(slotId), attributeModifier,
                                 SlotTypePredicate.builder().withId(slot).build()));
      return this;
    }

    public Builder addSlotModifier(String slotId, AttributeModifier attributeModifier,
                                   SlotTypePredicate slotTypePredicate) {
      this.entries.add(
          new Entry(SlotAttribute.getOrCreate(slotId), attributeModifier, slotTypePredicate));
      return this;
    }

    public Builder addModifier(Holder<Attribute> attribute, AttributeModifier attributeModifier) {
      this.entries.add(new Entry(attribute, attributeModifier, SlotTypePredicate.ANY));
      return this;
    }

    public Builder addModifier(Holder<Attribute> attribute, AttributeModifier attributeModifier,
                               String... slot) {
      this.entries.add(new Entry(attribute, attributeModifier,
                                 SlotTypePredicate.builder().withId(slot).build()));
      return this;
    }

    public Builder addModifier(Holder<Attribute> attribute, AttributeModifier attributeModifier,
                               SlotTypePredicate slotTypePredicate) {
      this.entries.add(new Entry(attribute, attributeModifier, slotTypePredicate));
      return this;
    }

    public CurioAttributeModifiers build() {
      return new CurioAttributeModifiers(this.entries.build(), true);
    }
  }

  public static class Entry {

    public static final Codec<Entry> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                Codec.withAlternative(Attribute.CODEC, SlotAttribute.CODEC)
                    .fieldOf("type")
                    .forGetter(Entry::attributeHolder),
                AttributeModifier.MAP_CODEC
                    .forGetter(Entry::modifier),
                SlotTypePredicate.FULL_CODEC
                    .optionalFieldOf("slot", SlotTypePredicate.ANY)
                    .forGetter(Entry::slotType))
            .apply(instance, Entry::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC =
        StreamCodec.composite(
            SlotAttribute.STREAM_CODEC,
            Entry::attributeHolder,
            AttributeModifier.STREAM_CODEC,
            Entry::modifier,
            SlotTypePredicate.STREAM_CODEC,
            Entry::slotType,
            Entry::new
        );

    private final Holder<Attribute> attributeHolder;
    private final AttributeModifier modifier;
    private final SlotTypePredicate slotType;

    public Entry(Holder<Attribute> attributeHolder, AttributeModifier modifier,
                 SlotTypePredicate slotType) {
      this.attributeHolder = attributeHolder;
      this.modifier = modifier;
      this.slotType = slotType;
    }

    @Deprecated(forRemoval = true)
    public Entry(ResourceLocation attribute, AttributeModifier modifier, String slot) {
      this.attributeHolder = BuiltInRegistries.ATTRIBUTE.get(attribute)
          .map(IHolderExtension::getDelegate).orElse(Attributes.ARMOR);
      this.modifier = modifier;
      this.slotType = SlotTypePredicate.builder().withId(slot).build();
    }

    public Holder<Attribute> attributeHolder() {
      return this.attributeHolder;
    }

    public AttributeModifier modifier() {
      return this.modifier;
    }

    public SlotTypePredicate slotType() {
      return this.slotType;
    }

    @Deprecated(forRemoval = true)
    public ResourceLocation attribute() {
      return BuiltInRegistries.ATTRIBUTE.getKey(this.attributeHolder.value());
    }

    @Deprecated(forRemoval = true)
    public String slot() {
      List<String> id = this.slotType.id();
      return id.isEmpty() ? "curio" : id.getFirst();
    }
  }
}
