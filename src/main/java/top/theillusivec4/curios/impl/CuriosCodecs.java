package top.theillusivec4.curios.impl;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import top.theillusivec4.curios.CuriosConstants;
import top.theillusivec4.curios.api.CuriosResources;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.internal.services.ICuriosCodecs;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.data.IEntitiesData;
import top.theillusivec4.curios.api.type.data.ISlotData;
import top.theillusivec4.curios.common.data.EntitiesData;
import top.theillusivec4.curios.common.data.SlotData;
import top.theillusivec4.curios.common.slot.SlotType;

public class CuriosCodecs implements ICuriosCodecs {

  @Override
  public Codec<ISlotType> slotTypeCodec() {
    return RecordCodecBuilder.create(
        instance -> instance.group(
                Codec.STRING
                    .fieldOf("id")
                    .forGetter(ISlotType::getId),
                Codec.INT
                    .optionalFieldOf("order", 0)
                    .forGetter(ISlotType::getOrder),
                Codec.INT
                    .optionalFieldOf("size", 1)
                    .forGetter(ISlotType::getSize),
                Codec.BOOL
                    .optionalFieldOf("use_native_gui", true)
                    .forGetter(ISlotType::useNativeGui),
                Codec.BOOL
                    .optionalFieldOf("add_cosmetic", false)
                    .forGetter(ISlotType::hasCosmetic),
                ResourceLocation.CODEC
                    .optionalFieldOf("icon", ISlotType.GENERIC_ICON)
                    .forGetter(ISlotType::getIcon),
                DropRule.CODEC
                    .optionalFieldOf("drop_rule", DropRule.DEFAULT)
                    .forGetter(ISlotType::getDropRule),
                Codec.BOOL
                    .optionalFieldOf("render_toggle", true)
                    .forGetter(ISlotType::canToggleRendering),
                ResourceLocation.CODEC.listOf()
                    .fieldOf("validators")
                    .xmap(list -> (Set<ResourceLocation>) new HashSet<>(list), ArrayList::new)
                    .forGetter(ISlotType::getValidators),
                Codec.STRING.listOf()
                    .xmap(list -> {
                      List<EntityType<?>> entityTypes = new ArrayList<>();
                      list.forEach(entityType -> {
                        if (entityType.startsWith("#")) {
                          TagKey<EntityType<?>> key = TagKey.create(Registries.ENTITY_TYPE,
                                                                    ResourceLocation.parse(
                                                                        entityType.substring(1)));
                          BuiltInRegistries.ENTITY_TYPE.get(key).ifPresent(holders -> {
                            holders.forEach(holder -> entityTypes.add(holder.value()));
                          });
                        } else {
                          BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityType))
                              .ifPresent(holder -> {
                                entityTypes.add(holder.value());
                              });
                        }
                      });
                      return entityTypes;
                    }, entityTypes -> {
                      List<String> names = new ArrayList<>();
                      entityTypes.forEach(entityType -> names.add(
                          BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString()));
                      return names;
                    })
                    .fieldOf("entities")
                    .xmap(list -> (Set<EntityType<?>>) new HashSet<>(list), ArrayList::new)
                    .forGetter(ISlotType::getDefaultEntityTypes))
            .apply(instance, SlotType::new));
  }

  @Override
  public Codec<ISlotData.Entry> slotDataEntryCodec() {
    final Codec<Either<TagKey<EntityType<?>>, ResourceKey<EntityType<?>>>> tagOrValue =
        ExtraCodecs.TAG_OR_ELEMENT_ID.xmap(
            l -> l.tag() ? Either.left(TagKey.create(Registries.ENTITY_TYPE, l.id()))
                         : Either.right(ResourceKey.create(Registries.ENTITY_TYPE, l.id())),
            e -> e.map(t -> new ExtraCodecs.TagOrElementLocation(t.location(), true),
                       r -> new ExtraCodecs.TagOrElementLocation(r.location(), false)));
    return RecordCodecBuilder.create(
        slot -> slot.group(
                Codec.BOOL
                    .optionalFieldOf("replace", false)
                    .forGetter(ISlotData.Entry::replace),
                Codec.STRING
                    .optionalFieldOf("id")
                    .forGetter(ISlotData.Entry::id),
                Codec.INT
                    .optionalFieldOf("order")
                    .forGetter(ISlotData.Entry::order),
                Codec.INT
                    .optionalFieldOf("size")
                    .forGetter(ISlotData.Entry::size),
                Codec.STRING
                    .optionalFieldOf("operation")
                    .forGetter(ISlotData.Entry::operation),
                Codec.BOOL
                    .optionalFieldOf("use_native_gui")
                    .forGetter(ISlotData.Entry::useNativeGui),
                Codec.BOOL
                    .optionalFieldOf("add_cosmetic")
                    .forGetter(ISlotData.Entry::hasCosmetic),
                ResourceLocation.CODEC
                    .optionalFieldOf("icon")
                    .forGetter(ISlotData.Entry::icon),
                DropRule.CODEC
                    .optionalFieldOf("drop_rule")
                    .forGetter(ISlotData.Entry::dropRule),
                Codec.BOOL
                    .optionalFieldOf("render_toggle")
                    .forGetter(ISlotData.Entry::renderToggle),
                ICondition.CODEC.listOf()
                    .optionalFieldOf(ConditionalOps.DEFAULT_CONDITIONS_KEY, List.of())
                    .forGetter(ISlotData.Entry::conditions),
                ResourceLocation.CODEC.listOf()
                    .optionalFieldOf("validators")
                    .forGetter(ISlotData.Entry::validators),
                tagOrValue.listOf()
                    .optionalFieldOf("entities")
                    .forGetter(ISlotData.Entry::entities))
            .apply(slot, SlotData.Entry::new)
    );
  }

  @Override
  public Codec<IEntitiesData.Entry> entitiesDataEntryCodec() {
    final Codec<Either<TagKey<EntityType<?>>, ResourceKey<EntityType<?>>>> tagOrValue =
        ExtraCodecs.TAG_OR_ELEMENT_ID.xmap(
            l -> l.tag() ? Either.left(TagKey.create(Registries.ENTITY_TYPE, l.id()))
                         : Either.right(ResourceKey.create(Registries.ENTITY_TYPE, l.id())),
            e -> e.map(t -> new ExtraCodecs.TagOrElementLocation(t.location(), true),
                       r -> new ExtraCodecs.TagOrElementLocation(r.location(), false)));
    return RecordCodecBuilder.create(
        slot -> slot.group(
                Codec.BOOL
                    .optionalFieldOf("replace", false)
                    .forGetter(IEntitiesData.Entry::replace),
                tagOrValue.listOf()
                    .fieldOf("entities")
                    .forGetter(IEntitiesData.Entry::entities),
                Codec.either(Codec.STRING, EntitiesData.EntitySlotEntry.CODEC)
                    .listOf()
                    .fieldOf("slots")
                    .forGetter(IEntitiesData.Entry::slots),
                ICondition.CODEC.listOf()
                    .optionalFieldOf(ConditionalOps.DEFAULT_CONDITIONS_KEY, List.of())
                    .forGetter(IEntitiesData.Entry::conditions)
            )
            .apply(slot, EntitiesData.Entry::new));
  }

  @Override
  public Codec<Holder<Attribute>> slotAttributeCodec() {
    return ResourceLocation.CODEC.xmap(
        resourceLocation -> {
          if (resourceLocation.getNamespace().startsWith(CuriosResources.MOD_ID)) {
            String key = resourceLocation.getPath();
            ISlotType slotType = ISlotType.get(key);

            if (slotType != null) {
              return SlotAttribute.getOrCreate(key);
            }
          }
          return SlotAttribute.getOrCreate(CuriosSlotTypes.Preset.CURIO.id());
        },
        attributeHolder -> {
          if (attributeHolder.value() instanceof SlotAttribute slotAttribute) {
            return CuriosResources.resource(slotAttribute.id());
          }
          return CuriosResources.resource(CuriosSlotTypes.Preset.CURIO.id());
        }
    );
  }

  @Override
  public StreamCodec<RegistryFriendlyByteBuf, Holder<Attribute>> slotAttributeStreamCodec() {
    return new StreamCodec<>() {

      @Nonnull
      @Override
      public Holder<Attribute> decode(@Nonnull RegistryFriendlyByteBuf buffer) {
        ResourceLocation resourceLocation = ResourceLocation.STREAM_CODEC.decode(buffer);

        if (resourceLocation.getNamespace().equals(CuriosConstants.MOD_ID)) {
          return SlotAttribute.getOrCreate(resourceLocation.getPath());
        }
        return BuiltInRegistries.ATTRIBUTE.getOrThrow(
            ResourceKey.create(Registries.ATTRIBUTE, resourceLocation));
      }

      @Override
      public void encode(@Nonnull RegistryFriendlyByteBuf buffer,
                         @Nonnull Holder<Attribute> value) {
        ResourceLocation resourceLocation;

        if (value.value() instanceof SlotAttribute slotAttribute) {
          resourceLocation = slotAttribute.resourceLocation();
        } else {
          resourceLocation = BuiltInRegistries.ATTRIBUTE.getKey(value.value());
        }
        ResourceLocation.STREAM_CODEC.encode(buffer, Objects.requireNonNull(resourceLocation));
      }
    };
  }

  @Override
  public StreamCodec<RegistryFriendlyByteBuf, ISlotType> slotTypeStreamCodec() {
    return new StreamCodec<>() {

      @Nonnull
      @Override
      public ISlotType decode(@Nonnull RegistryFriendlyByteBuf buffer) {
        String id = ByteBufCodecs.STRING_UTF8.decode(buffer);
        int order = ByteBufCodecs.INT.decode(buffer);
        int size = ByteBufCodecs.INT.decode(buffer);
        boolean useNativeGui = ByteBufCodecs.BOOL.decode(buffer);
        boolean hasCosmetic = ByteBufCodecs.BOOL.decode(buffer);
        ResourceLocation icon = ResourceLocation.STREAM_CODEC.decode(buffer);
        DropRule dropRule = DropRule.STREAM_CODEC.decode(buffer);
        boolean renderToggle = ByteBufCodecs.BOOL.decode(buffer);
        Set<EntityType<?>> entityTypes =
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.registry(Registries.ENTITY_TYPE),
                                     256).decode(buffer);
        Set<ResourceLocation> validators =
            ByteBufCodecs.collection(HashSet::new, ResourceLocation.STREAM_CODEC, 256)
                .decode(buffer);
        return new SlotType(id, order, size, useNativeGui, hasCosmetic, icon, dropRule,
                            renderToggle, validators, entityTypes);
      }

      @Override
      public void encode(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull ISlotType value) {
        ByteBufCodecs.STRING_UTF8.encode(buffer, value.getId());
        ByteBufCodecs.INT.encode(buffer, value.getOrder());
        ByteBufCodecs.INT.encode(buffer, value.getSize());
        ByteBufCodecs.BOOL.encode(buffer, value.useNativeGui());
        ByteBufCodecs.BOOL.encode(buffer, value.hasCosmetic());
        ResourceLocation.STREAM_CODEC.encode(buffer, value.getIcon());
        DropRule.STREAM_CODEC.encode(buffer, value.getDropRule());
        ByteBufCodecs.BOOL.encode(buffer, value.canToggleRendering());
        ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.registry(Registries.ENTITY_TYPE), 256)
            .encode(buffer, new HashSet<>(value.getDefaultEntityTypes()));
        ByteBufCodecs.collection(HashSet::new, ResourceLocation.STREAM_CODEC, 256)
            .encode(buffer, new HashSet<>(value.getValidators()));
      }
    };
  }
}
