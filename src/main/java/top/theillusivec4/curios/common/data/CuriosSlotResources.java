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

package top.theillusivec4.curios.common.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import top.theillusivec4.curios.CuriosConstants;
import top.theillusivec4.curios.api.CuriosResources;
import top.theillusivec4.curios.api.CuriosTags;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.data.IEntitiesData;
import top.theillusivec4.curios.api.type.data.ISlotData;
import top.theillusivec4.curios.common.slot.SlotType;
import top.theillusivec4.curios.config.CuriosConfig;

public class CuriosSlotResources extends SimpleJsonResourceReloadListener<JsonElement> {

  private static final String folder = "curios";

  public static final ResourceLocation ID = CuriosResources.resource("curios_slots");

  public static CuriosSlotResources SERVER;
  public static CuriosSlotResources CLIENT = new CuriosSlotResources();

  public static final StreamCodec<RegistryFriendlyByteBuf, CuriosSlotResources> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.map(
              HashMap::new,
              ByteBufCodecs.registry(Registries.ENTITY_TYPE),
              ByteBufCodecs.collection(
                  HashSet::new,
                  ByteBufCodecs.STRING_UTF8
              )
          ),
          curiosEntityResources -> {
            Map<EntityType<?>, Map<String, ISlotType>> map = curiosEntityResources.entitySlots;
            Map<EntityType<?>, Set<String>> result = new HashMap<>();
            for (Map.Entry<EntityType<?>, Map<String, ISlotType>> entry : map.entrySet()) {
              result.put(entry.getKey(), entry.getValue().keySet());
            }
            return result;
          },
          ByteBufCodecs.map(
              HashMap::new,
              ByteBufCodecs.STRING_UTF8,
              ISlotType.STREAM_CODEC
          ),
          CuriosSlotResources::getSlots,
          ByteBufCodecs.map(
              HashMap::new,
              ByteBufCodecs.STRING_UTF8,
              ByteBufCodecs.collection(
                  HashSet::new,
                  ByteBufCodecs.STRING_UTF8
              )
          ),
          CuriosSlotResources::getModsFromSlots,
          CuriosSlotResources::new
      );

  private RegistryAccess registryAccess;
  private Map<ResourceLocation, JsonElement> pendingData = Map.of();
  private Map<String, ISlotType> slots = ImmutableMap.of();
  private Map<EntityType<?>, Map<String, ISlotType>> entitySlots = ImmutableMap.of();
  private Set<String> configSlots = ImmutableSet.of();
  private Map<String, Set<String>> idToMods = ImmutableMap.of();

  public CuriosSlotResources() {
    super(ExtraCodecs.JSON, FileToIdConverter.json(folder));
  }

  public CuriosSlotResources(RegistryAccess registryAccess) {
    super(ExtraCodecs.JSON, FileToIdConverter.json(folder));
    this.registryAccess = registryAccess;
  }

  public CuriosSlotResources(Map<EntityType<?>, Set<String>> entitySlots,
                             Map<String, ISlotType> slots, Map<String, Set<String>> idToMods) {
    super(ExtraCodecs.JSON, FileToIdConverter.json(folder));
    this.slots = slots;
    Map<EntityType<?>, Map<String, ISlotType>> newEntitySlots = new LinkedHashMap<>();
    entitySlots.forEach((k, v) -> {
      Map<String, ISlotType> slotTypes = newEntitySlots.computeIfAbsent(k, (k1) -> new HashMap<>());
      for (String s : v) {
        ISlotType slotType = slots.get(s);

        if (slotType != null) {
          slotTypes.put(s, slotType);
        }
      }
    });
    this.entitySlots = ImmutableMap.copyOf(newEntitySlots);
    this.idToMods = ImmutableMap.copyOf(idToMods);
  }

  protected void apply(Map<ResourceLocation, JsonElement> object,
                       @Nonnull ResourceManager resourceManager,
                       @Nonnull ProfilerFiller profiler) {
    Map<ResourceLocation, JsonElement> sorted = new TreeMap<>((o1, o2) -> {
      String s1 = o1.getNamespace();
      String s2 = o2.getNamespace();

      if (s1.equals(CuriosConstants.MOD_ID) && !s2.equals(CuriosConstants.MOD_ID)) {
        return -1;
      } else if (s2.equals(CuriosConstants.MOD_ID) && !s1.equals(CuriosConstants.MOD_ID)) {
        return 1;
      } else {
        return o1.compareTo(o2);
      }
    });
    sorted.putAll(object);
    this.pendingData = sorted;
  }

  public void populateData() {
    Map<String, SlotType.Builder> slotMap = new HashMap<>();
    Map<EntityType<?>, ImmutableSet.Builder<String>> entityMap = new HashMap<>();
    Map<String, ImmutableSet.Builder<String>> modMap = new HashMap<>();
    HolderLookup.RegistryLookup<EntityType<?>> registry =
        this.registryAccess.lookupOrThrow(Registries.ENTITY_TYPE);

    // First parse through the slot data files
    for (Map.Entry<ResourceLocation, JsonElement> entry : this.pendingData.entrySet()) {

      if (!entry.getKey().getPath().startsWith("slots")) {
        continue;
      }
      String namespace = entry.getKey().getNamespace();
      String id = entry.getKey().getPath().substring("slots/".length());
      ISlotData.Entry.CODEC.decode(
              this.registryAccess.createSerializationContext(JsonOps.INSTANCE), entry.getValue())
          .ifSuccess(pair -> {
            ISlotData.Entry slotDataEntry = pair.getFirst();
            slotDataEntry.entities().ifPresent(entities -> {
              List<EntityType<?>> list = getEntitiesFromEither(registry, entities);

              for (EntityType<?> entityType : list) {
                entityMap.computeIfAbsent(entityType, (k) -> ImmutableSet.builder())
                    .add(id);
              }
            });
            slotMap.computeIfAbsent(id, SlotType.Builder::new)
                .apply(slotDataEntry, this.registryAccess);
            modMap.computeIfAbsent(id, (k) -> ImmutableSet.builder())
                .add(namespace);
          });
    }

    // Secondly parse through the config slot data
    try {
      Set<String> configs = fromConfig(slotMap, this.registryAccess);
      this.configSlots = ImmutableSet.copyOf(configs);

      for (String id : configs) {
        modMap.computeIfAbsent(id, (k) -> ImmutableSet.builder()).add("config");
        // Assume player-like entities for config entries
        registry.get(CuriosTags.PLAYER_LIKE).ifPresent(entries -> {

          for (Holder<EntityType<?>> entry : entries) {
            entityMap.computeIfAbsent(entry.value(), (k) -> ImmutableSet.builder()).add(id);
          }
        });
      }
    } catch (IllegalArgumentException e) {
      CuriosConstants.LOG.error("Config parsing error", e);
    }

    for (Map.Entry<ResourceLocation, JsonElement> entry : this.pendingData.entrySet()) {

      if (!entry.getKey().getPath().startsWith("entities")) {
        continue;
      }
      ResourceLocation resourcelocation = entry.getKey();
      IEntitiesData.Entry.CODEC.decode(
              this.registryAccess.createSerializationContext(JsonOps.INSTANCE), entry.getValue())
          .ifSuccess(pair -> {
            IEntitiesData.Entry entityDataEntry = pair.getFirst();
            List<EntityType<?>> entities =
                getEntitiesFromEither(registry, entityDataEntry.entities());

            for (EntityType<?> entity : entities) {
              ImmutableSet.Builder<String> builder =
                  entityDataEntry.replace()
                  ? entityMap.computeIfPresent(entity, (k, v) -> ImmutableSet.builder())
                  : entityMap.computeIfAbsent(entity, k -> ImmutableSet.builder());
              List<Either<String, IEntitiesData.IEntitySlotEntry>> slots = entityDataEntry.slots();

              for (Either<String, IEntitiesData.IEntitySlotEntry> slot : slots) {
                IEntitiesData.IEntitySlotEntry slotEntry = slot.map(
                    str -> new EntitiesData.EntitySlotEntry(true, new SlotData(str, true).build()),
                    c -> c);
                String key = slotEntry.slot().id().orElse("");

                if (!key.isEmpty()) {
                  boolean create = slotEntry.create();
                  SlotType.Builder slotType = slotMap.get(key);

                  if (create && slotType == null) {
                    SlotType.Builder slotBuilder = new SlotType.Builder(key);
                    slotBuilder.apply(slotEntry.slot(), this.registryAccess);
                    slotType = slotMap.put(key, slotBuilder);
                  }

                  if (slotType != null) {
                    Objects.requireNonNull(builder).add(key);
                  }
                }
              }
            }
          });
      modMap.computeIfAbsent(resourcelocation.getPath(), (k) -> ImmutableSet.builder())
          .add(resourcelocation.getNamespace());
    }
    this.slots = slotMap.entrySet().stream()
        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> entry.getValue().build()));
    this.idToMods = modMap.entrySet().stream()
        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> entry.getValue().build()));
    Map<EntityType<?>, Map<String, ISlotType>> newEntitySlots = new LinkedHashMap<>();

    for (Map.Entry<EntityType<?>, ImmutableSet.Builder<String>> entry : entityMap.entrySet()) {
      Set<String> slots = entry.getValue().build();
      Map<String, ISlotType> innerMap = new LinkedHashMap<>();

      for (String id : slots) {
        ISlotType slotType = this.slots.get(id);

        if (slotType != null) {
          innerMap.put(id, slotType);
        }
      }
      newEntitySlots.put(entry.getKey(), innerMap);
    }
    this.entitySlots = ImmutableMap.copyOf(newEntitySlots);
    CuriosConstants.LOG.info("Loaded {} curio slots", slotMap.size());
    CuriosConstants.LOG.info("Loaded {} curio entities", entityMap.size());
    this.pendingData.clear();
  }

  private List<EntityType<?>> getEntitiesFromEither(
      HolderLookup.RegistryLookup<EntityType<?>> lookup,
      List<Either<TagKey<EntityType<?>>, ResourceKey<EntityType<?>>>> resource) {
    List<EntityType<?>> entities = new ArrayList<>();
    for (Either<TagKey<EntityType<?>>, ResourceKey<EntityType<?>>> entry : resource) {
      entry.ifRight(entity -> {
        lookup.get(entity).ifPresent(val -> entities.add(val.value()));
      });
      entry.ifLeft(entityTag -> {
        lookup.get(entityTag).ifPresent(val -> {
          for (Holder<EntityType<?>> entityTypeHolder : val) {
            entities.add(entityTypeHolder.value());
          }
        });
      });
    }
    return entities;
  }

  public void setAllEntitySlots(Map<EntityType<?>, Map<String, ISlotType>> slots) {
    this.entitySlots = slots;
  }

  public Map<EntityType<?>, Map<String, ISlotType>> getAllEntitySlots() {
    return this.entitySlots;
  }

  public Map<String, ISlotType> getPlayerSlots() {
    return this.getEntitySlots(EntityType.PLAYER);
  }

  public Map<String, ISlotType> getEntitySlots(EntityType<?> type) {

    if (this.entitySlots.containsKey(type)) {
      return this.entitySlots.get(type);
    }
    return ImmutableMap.of();
  }

  public Map<String, Set<String>> getModsFromSlots() {
    return this.idToMods;
  }


  public Map<String, ISlotType> getSlots() {
    return this.slots;
  }

  public void setSlots(Map<String, ISlotType> slots) {
    this.slots = slots;
  }

  @Nullable
  public ISlotType getSlot(String id) {
    return this.slots.get(id);
  }

  public Set<String> getConfigSlots() {
    return this.configSlots;
  }

  public static Set<String> fromConfig(Map<String, SlotType.Builder> map,
                                       HolderLookup.Provider provider)
      throws IllegalArgumentException {
    List<Map<String, String>> parsed = new ArrayList<>();
    List<? extends String> list = CuriosConfig.COMMON.slots.get();
    Set<String> results = new HashSet<>();

    for (String s : list) {
      StringTokenizer tokenizer = new StringTokenizer(s, ";");
      Map<String, String> subMap = new HashMap<>();

      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken();
        String[] keyValue = token.split("=");
        subMap.put(keyValue[0], keyValue[1]);
      }

      if (subMap.containsKey("id")) {
        parsed.add(subMap);
      } else {
        throw new IllegalArgumentException(
            "Cannot load config entry " + s + " due to missing id field");
      }
    }

    for (Map<String, String> entry : parsed) {
      String id = entry.get("id");
      ISlotData.Entry slotDataEntry = new SlotData.Entry(
          true,
          Optional.of(id),
          getValueOptionally(entry, "order", Integer::parseInt),
          getValueOptionally(entry, "size", Integer::parseInt),
          getValueOptionally(entry, "operation", String::toString),
          getValueOptionally(entry, "use_native_gui", Boolean::parseBoolean),
          getValueOptionally(entry, "add_cosmetic", Boolean::parseBoolean),
          getValueOptionally(entry, "icon", ResourceLocation::tryParse),
          getValueOptionally(entry, "drop_rule", dropRule -> {
            for (DropRule value : DropRule.values()) {
              if (dropRule.equalsIgnoreCase(value.getSerializedName())) {
                return value;
              }
            }
            return DropRule.DEFAULT;
          }),
          getValueOptionally(entry, "toggle_render", Boolean::parseBoolean),
          List.of(),
          Optional.empty(),
          Optional.empty());
      map.computeIfAbsent(id, SlotType.Builder::new).apply(slotDataEntry, provider);
      results.add(id);
    }
    return results;
  }

  private static <T> Optional<T> getValueOptionally(Map<String, String> map, String key,
                                                    Function<String, T> mapper) {
    if (map.containsKey(key)) {
      return Optional.of(mapper.apply(map.get(key)));
    }
    return Optional.empty();
  }
}
