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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.conditions.ICondition;
import top.theillusivec4.curios.CuriosConstants;
import top.theillusivec4.curios.api.type.ISlotType;

public class CuriosEntityManager extends SimpleJsonResourceReloadListener {

  private static final Gson GSON =
      (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

  public static CuriosEntityManager SERVER = new CuriosEntityManager();
  public static CuriosEntityManager CLIENT = new CuriosEntityManager();
  private Map<EntityType<?>, Map<String, ISlotType>> entitySlots = ImmutableMap.of();
  private Map<String, Set<String>> idToMods = ImmutableMap.of();

  private final Map<EntityType<?>, ImmutableMap.Builder<String, ISlotType>> pendingEntitySlots = new HashMap<>();
  private final Map<ResourceLocation, ImmutableMap.Builder<String, ISlotType>> pendingTaggedEntitySlots = new HashMap<>();

  public CuriosEntityManager() {
    super(GSON, "curios/entities");
  }

  @Override
  protected void apply(
      @Nonnull Map<ResourceLocation, JsonElement> pObject,
      @Nonnull ResourceManager pResourceManager,
      @Nonnull ProfilerFiller pProfiler) {
    Map<ResourceLocation, JsonElement> sortedEntityAssignments = new LinkedHashMap<>();
    pResourceManager.listPacks().forEach(packResources -> {
      Set<String> namespaces = packResources.getNamespaces(PackType.SERVER_DATA);
      namespaces.forEach(
          namespace -> packResources.listResources(PackType.SERVER_DATA, namespace,
              "curios/entities",
              (resourceLocation, inputStreamIoSupplier) -> {
                String path = resourceLocation.getPath();
                ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(namespace,
                    path.substring("curios/entities/".length(), path.length() - ".json".length()));
                JsonElement el = pObject.get(rl);
                if (el != null) {
                  sortedEntityAssignments.put(rl, el);
                }
              }));
    });
    this.pendingEntitySlots.clear();
    this.pendingTaggedEntitySlots.clear();
    Map<String, ImmutableSet.Builder<String>> namespacesByDefinitionId = new HashMap<>();

    for (Map.Entry<ResourceLocation, JsonElement> entityAssignmentEntry : sortedEntityAssignments.entrySet()) {
      ResourceLocation assignmentLocation = entityAssignmentEntry.getKey();

      if (assignmentLocation.getPath().startsWith("_")) {
        continue;
      }

      try {
        JsonObject assignmentJson = GsonHelper.convertToJsonObject(entityAssignmentEntry.getValue(), "top element");

        if (ICondition.conditionsMatched(JsonOps.INSTANCE, assignmentJson)) {
          Map<String, ISlotType> parsedSlots = parseSlots(assignmentJson);
          Pair<Set<EntityType<?>>, Set<ResourceLocation>> parsedEntities = parseEntities(assignmentJson);
          boolean replace = GsonHelper.getAsBoolean(assignmentJson, "replace", false);

          for (EntityType<?> entityType : parsedEntities.getFirst()) {

            if (replace) {
              ImmutableMap.Builder<String, ISlotType> slotBuilder = ImmutableMap.builder();
              slotBuilder.putAll(parsedSlots);
              this.pendingEntitySlots.put(entityType, slotBuilder);
            } else {
              this.pendingEntitySlots
                  .computeIfAbsent(entityType, (k) -> ImmutableMap.builder())
                  .putAll(parsedSlots);
            }
          }

          for (ResourceLocation resourceLocation : parsedEntities.getSecond()) {

            if (replace) {
              ImmutableMap.Builder<String, ISlotType> slotBuilder = ImmutableMap.builder();
              slotBuilder.putAll(parsedSlots);
              this.pendingTaggedEntitySlots.put(resourceLocation, slotBuilder);
            } else {
              this.pendingTaggedEntitySlots
                  .computeIfAbsent(resourceLocation, (k) -> ImmutableMap.builder())
                  .putAll(parsedSlots);
            }
          }
          namespacesByDefinitionId
              .computeIfAbsent(assignmentLocation.getPath(), (k) -> ImmutableSet.builder())
              .add(assignmentLocation.getNamespace());
        } else {
          CuriosConstants.LOG.debug(
              "Skipping loading entity file {} as its conditions were not met",
              assignmentLocation);
        }
      } catch (IllegalArgumentException | JsonParseException e) {
        CuriosConstants.LOG.error("Failed to parse curio entity slot assignment {}", assignmentLocation, e);
      }
    }
    Map<String, ISlotType> configuredSlotTypes = new HashMap<>();

    for (String configuredSlotId : CuriosSlotManager.SERVER.getConfigSlots()) {
      CuriosSlotManager.SERVER
          .getSlot(configuredSlotId)
          .ifPresentOrElse(
              (slot) -> configuredSlotTypes.put(configuredSlotId, slot),
              () -> CuriosConstants.LOG.error("Configured slot type '{}' is missing from the server slot registry", configuredSlotId)
          );
    }
    ImmutableMap.Builder<String, ISlotType> playerSlotBuilder =
        this.pendingEntitySlots.computeIfAbsent(
            EntityType.PLAYER,
            (k) -> ImmutableMap.builder());
    playerSlotBuilder.putAll(configuredSlotTypes);
    this.idToMods = namespacesByDefinitionId
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Map.Entry::getKey,
                entry -> entry.getValue().build()));
    this.entitySlots = this.pendingEntitySlots
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Map.Entry::getKey,
                entry -> entry.getValue().buildKeepingLast()));
  }

  private static Map<String, ISlotType> parseSlots(JsonObject jsonObject) {
    JsonArray jsonSlots = GsonHelper.getAsJsonArray(jsonObject, "slots", new JsonArray());
    Map<String, ISlotType> slots = new HashMap<>();

    for (JsonElement jsonSlot : jsonSlots) {
      String id = jsonSlot.getAsString();
      CuriosSlotManager.SERVER.getSlot(id).ifPresentOrElse(slot -> slots.put(id, slot),
          () -> CuriosConstants.LOG.error("{} is not a registered slot type!", id));
    }
    return slots;
  }

  private static Pair<Set<EntityType<?>>, Set<ResourceLocation>> parseEntities(JsonObject jsonObject) {
    JsonArray jsonEntities = GsonHelper.getAsJsonArray(jsonObject, "entities", new JsonArray());
    Set<EntityType<?>> addEntities = new HashSet<>();
    Set<ResourceLocation> addTags = new HashSet<>();

    for (JsonElement jsonEntity : jsonEntities) {
      String entity = jsonEntity.getAsString();

      if (entity.startsWith("#")) {
        addTags.add(ResourceLocation.parse(entity.substring(1)));
      } else {
        EntityType<?> type =
            BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(entity)).orElse(null);

        if (type != null) {
          addEntities.add(type);
        } else {
          CuriosConstants.LOG.error("{} is not a registered entity type!", entity);
        }
      }
    }
    return Pair.of(addEntities, addTags);
  }

  public void buildEntitySlotsWithTags(Registry<EntityType<?>> entityTypeRegistry) {

    // An unexpected empty state in the pre-built entity slot map shouldn't process tagged entities
    // into the map either.
    if (this.entitySlots.isEmpty()) {
      return;
    }

    for (Map.Entry<ResourceLocation, ImmutableMap.Builder<String, ISlotType>> entry :
        this.pendingTaggedEntitySlots.entrySet()) {
      entityTypeRegistry.getTag(TagKey.create(Registries.ENTITY_TYPE, entry.getKey()))
          .ifPresentOrElse(
              named -> {
                Map<String, ISlotType> slots = entry.getValue().build();

                for (Holder<EntityType<?>> entityTypeHolder : named) {
                  this.pendingEntitySlots
                      .computeIfAbsent(entityTypeHolder.value(), (k) -> ImmutableMap.builder())
                      .putAll(slots);
                }
              },
              () -> { // Couldn't find the entity type tag
                CuriosConstants.LOG.error("Couldn't find entity type tag: {}!", entry.getKey());
              });
    }
    this.entitySlots = this.pendingEntitySlots
        .entrySet()
        .stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Map.Entry::getKey,
                entry -> entry.getValue().buildKeepingLast()));
    CuriosConstants.LOG.info("Loaded {} curio entities", this.pendingEntitySlots.size());
    this.pendingEntitySlots.clear();
    this.pendingTaggedEntitySlots.clear();
  }

  public static ListTag getSyncPacket() {
    ListTag tag = new ListTag();

    for (Map.Entry<EntityType<?>, Map<String, ISlotType>> entry : SERVER.entitySlots.entrySet()) {
      ResourceLocation rl = BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey());

      CompoundTag entity = new CompoundTag();
      entity.putString("Entity", rl.toString());
      ListTag tag1 = new ListTag();

      for (Map.Entry<String, ISlotType> val : entry.getValue().entrySet()) {
        tag1.add(StringTag.valueOf(val.getKey()));
      }
      entity.put("Slots", tag1);
      tag.add(entity);
    }
    return tag;
  }

  public static void applySyncPacket(ListTag tag) {
    Map<EntityType<?>, ImmutableMap.Builder<String, ISlotType>> map = new HashMap<>();

    for (Tag tag1 : tag) {

      if (tag1 instanceof CompoundTag entity) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(
            ResourceLocation.parse(entity.getString("Entity"))).orElse(null);

        if (type != null) {
          ListTag slots = entity.getList("Slots", Tag.TAG_STRING);

          for (Tag slot : slots) {

            if (slot instanceof StringTag stringTag) {
              String id = stringTag.getAsString();
              CuriosSlotManager.CLIENT.getSlot(id).ifPresent(
                  slotType -> map.computeIfAbsent(type, (k) -> ImmutableMap.builder())
                      .put(id, slotType));
            }
          }
        }
      }
    }
    CLIENT.entitySlots = map.entrySet().stream().collect(
        ImmutableMap.toImmutableMap(Map.Entry::getKey, (entry) -> entry.getValue().build()));
  }

  public Map<String, ISlotType> getEntitySlots(EntityType<?> type) {

    if (this.entitySlots.containsKey(type)) {
      return this.entitySlots.get(type);
    }
    return ImmutableMap.of();
  }

  public Map<String, Set<String>> getModsFromSlots() {
    return ImmutableMap.copyOf(idToMods);
  }
}
