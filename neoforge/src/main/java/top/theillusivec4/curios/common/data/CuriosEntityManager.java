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
import com.mojang.serialization.JsonOps;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.core.Holder;
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

  public CuriosEntityManager() {
    super(GSON, "curios/entities");
  }

  protected void apply(Map<ResourceLocation, JsonElement> pObject,
                       @Nonnull ResourceManager pResourceManager,
                       @Nonnull ProfilerFiller pProfiler) {
    Map<EntityType<?>, ImmutableMap.Builder<String, ISlotType>> map = new HashMap<>();
    Map<String, ImmutableSet.Builder<String>> modMap = new HashMap<>();
    Map<ResourceLocation, JsonElement> sorted = new LinkedHashMap<>();
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
                  sorted.put(rl, el);
                }
              }));
    });

    for (Map.Entry<ResourceLocation, JsonElement> entry : sorted.entrySet()) {
      ResourceLocation resourcelocation = entry.getKey();

      if (resourcelocation.getPath().startsWith("_")) {
        continue;
      }

      try {
        JsonObject jsonObject = GsonHelper.convertToJsonObject(entry.getValue(), "top element");

        for (Map.Entry<EntityType<?>, Map<String, ISlotType>> entry1 : getSlotsForEntities(
            jsonObject, resourcelocation).entrySet()) {

          if (GsonHelper.getAsBoolean(jsonObject, "replace", false)) {
            ImmutableMap.Builder<String, ISlotType> builder = ImmutableMap.builder();
            builder.putAll(entry1.getValue());
            map.put(entry1.getKey(), builder);
          } else {
            map.computeIfAbsent(entry1.getKey(), (k) -> ImmutableMap.builder())
                .putAll(entry1.getValue());
          }
          modMap.computeIfAbsent(resourcelocation.getPath(), (k) -> ImmutableSet.builder())
              .add(resourcelocation.getNamespace());
        }
      } catch (IllegalArgumentException | JsonParseException e) {
        CuriosConstants.LOG.error("Parsing error loading curio entity {}", resourcelocation, e);
      }
    }
    Map<String, ISlotType> configSlots = new HashMap<>();

    for (String configSlot : CuriosSlotManager.SERVER.getConfigSlots()) {
      CuriosSlotManager.SERVER.getSlot(configSlot)
          .ifPresentOrElse(slot -> configSlots.put(configSlot, slot),
              () -> CuriosConstants.LOG.error("{} is not a registered slot type!", configSlot));
    }
    map.computeIfAbsent(EntityType.PLAYER, (k) -> ImmutableMap.builder()).putAll(configSlots);
    this.entitySlots = map.entrySet().stream().collect(
        ImmutableMap.toImmutableMap(Map.Entry::getKey,
            (entry) -> entry.getValue().buildKeepingLast()));
    this.idToMods = modMap.entrySet().stream()
        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> entry.getValue().build()));
    CuriosConstants.LOG.info("Loaded {} curio entities", map.size());
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

  private static Map<EntityType<?>, Map<String, ISlotType>> getSlotsForEntities(
      JsonObject jsonObject, ResourceLocation resourceLocation) {
    Map<EntityType<?>, Map<String, ISlotType>> map = new HashMap<>();

    if (!ICondition.conditionsMatched(JsonOps.INSTANCE, jsonObject)) {
      CuriosConstants.LOG.debug("Skipping loading entity file {} as its conditions were not met",
          resourceLocation);
      return map;
    }
    JsonArray jsonEntities = GsonHelper.getAsJsonArray(jsonObject, "entities", new JsonArray());
    Set<EntityType<?>> toAdd = new HashSet<>();

    for (JsonElement jsonEntity : jsonEntities) {
      String entity = jsonEntity.getAsString();

      if (entity.startsWith("#")) {
        BuiltInRegistries.ENTITY_TYPE.getTag(
                TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(entity)))
            .ifPresent(named -> {
              for (Holder<EntityType<?>> entityTypeHolder : named) {
                toAdd.add(entityTypeHolder.value());
              }
            });
      } else {
        EntityType<?> type =
            BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(entity)).orElse(null);

        if (type != null) {
          toAdd.add(type);
        } else {
          CuriosConstants.LOG.error("{} is not a registered entity type!", entity);
        }
      }
    }
    JsonArray jsonSlots = GsonHelper.getAsJsonArray(jsonObject, "slots", new JsonArray());
    Map<String, ISlotType> slots = new HashMap<>();

    for (JsonElement jsonSlot : jsonSlots) {
      String id = jsonSlot.getAsString();
      CuriosSlotManager.SERVER.getSlot(id).ifPresentOrElse(slot -> slots.put(id, slot),
          () -> CuriosConstants.LOG.error("{} is not a registered slot type!", id));
    }

    for (EntityType<?> entityType : toAdd) {
      map.computeIfAbsent(entityType, (k) -> new HashMap<>()).putAll(slots);
    }
    return map;
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
