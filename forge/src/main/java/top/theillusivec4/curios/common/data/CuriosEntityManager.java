/*
 * Copyright (c) 2018-2023 C4
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
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.CuriosConstants;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.common.slottype.LegacySlotManager;
import top.theillusivec4.curios.common.slottype.SlotType;

public class CuriosEntityManager extends SimpleJsonResourceReloadListener {

  private static final Gson GSON =
      (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

  public static CuriosEntityManager INSTANCE = new CuriosEntityManager();
  private Map<EntityType<?>, Map<String, ISlotType>> server = ImmutableMap.of();
  private Map<String, Set<String>> idToMods = ImmutableMap.of();
  private Map<EntityType<?>, Map<String, Integer>> client = ImmutableMap.of();
  private ICondition.IContext ctx = ICondition.IContext.EMPTY;

  public CuriosEntityManager() {
    super(GSON, "curios/entities");
  }

  public CuriosEntityManager(ICondition.IContext ctx) {
    super(GSON, "curios/entities");
    this.ctx = ctx;
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
                ResourceLocation rl = new ResourceLocation(namespace,
                    path.substring("curios/entities/".length(), path.length() - ".json".length()));
                JsonElement el = pObject.get(rl);
                if (el != null) {
                  sorted.put(rl, el);
                }
              }));
    });

    // Legacy IMC slot registrations - players only
    for (String s : LegacySlotManager.getImcBuilders().keySet()) {
      ImmutableMap.Builder<String, ISlotType> builder =
          map.computeIfAbsent(EntityType.PLAYER, (k) -> ImmutableMap.builder());
      CuriosSlotManager.INSTANCE.getSlot(s).ifPresentOrElse(slot -> builder.put(s, slot),
          () -> CuriosConstants.LOG.error("{} is not a registered slot type!", s));
    }

    for (Map.Entry<ResourceLocation, JsonElement> entry : sorted.entrySet()) {
      ResourceLocation resourcelocation = entry.getKey();

      if (resourcelocation.getPath().startsWith("_")) {
        continue;
      }

      try {
        JsonObject jsonObject = GsonHelper.convertToJsonObject(entry.getValue(), "top element");

        for (Map.Entry<EntityType<?>, Map<String, ISlotType>> entry1 : getSlotsForEntities(
            jsonObject, resourcelocation, this.ctx).entrySet()) {

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
    this.server = map.entrySet().stream().collect(
        ImmutableMap.toImmutableMap(Map.Entry::getKey,
            (entry) -> entry.getValue().buildKeepingLast()));
    this.idToMods = modMap.entrySet().stream()
        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> entry.getValue().build()));
    CuriosConstants.LOG.info("Loaded {} curio entities", map.size());
  }

  public static ListTag getSyncPacket() {
    ListTag tag = new ListTag();

    for (Map.Entry<EntityType<?>, Map<String, ISlotType>> entry : INSTANCE.server.entrySet()) {
      ResourceLocation rl = ForgeRegistries.ENTITY_TYPES.getKey(entry.getKey());

      if (rl != null) {
        CompoundTag entity = new CompoundTag();
        entity.putString("Entity", rl.toString());
        CompoundTag tag1 = new CompoundTag();

        for (Map.Entry<String, ISlotType> val : entry.getValue().entrySet()) {
          tag1.put(val.getKey(), IntTag.valueOf(val.getValue().getSize()));
        }
        entity.put("Slots", tag1);
        tag.add(entity);
      }
    }
    return tag;
  }

  public static void applySyncPacket(ListTag tag) {
    Map<EntityType<?>, ImmutableMap.Builder<String, Integer>> map = new HashMap<>();

    for (Tag tag1 : tag) {

      if (tag1 instanceof CompoundTag entity) {
        EntityType<?> type =
            ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entity.getString("Entity")));

        if (type != null) {
          CompoundTag slots = entity.getCompound("Slots");

          for (String key : slots.getAllKeys()) {
            int size = slots.getInt(key);
            map.computeIfAbsent(type, (k) -> ImmutableMap.builder()).put(key, size);
          }
        }
      }
    }
    INSTANCE.client = map.entrySet().stream().collect(
        ImmutableMap.toImmutableMap(Map.Entry::getKey, (entry) -> entry.getValue().build()));
  }

  private static Map<EntityType<?>, Map<String, ISlotType>> getSlotsForEntities(
      JsonObject jsonObject, ResourceLocation resourceLocation, ICondition.IContext ctx) {
    Map<EntityType<?>, Map<String, ISlotType>> map = new HashMap<>();

    if (!readAndTestCondition(jsonObject, ctx)) {
      CuriosConstants.LOG.debug("Skipping loading entity file {} as its conditions were not met",
          resourceLocation);
      return map;
    }
    JsonArray jsonEntities = GsonHelper.getAsJsonArray(jsonObject, "entities", new JsonArray());
    ITagManager<EntityType<?>> tagManager =
        Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.tags());
    Set<EntityType<?>> toAdd = new HashSet<>();

    for (JsonElement jsonEntity : jsonEntities) {
      String entity = jsonEntity.getAsString();

      if (entity.startsWith("#")) {

        for (EntityType<?> entityType : tagManager.getTag(
            tagManager.createTagKey(new ResourceLocation(entity)))) {
          toAdd.add(entityType);
        }
      } else {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entity));

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
      CuriosSlotManager.INSTANCE.getSlot(id).ifPresentOrElse(slot -> slots.put(id, slot),
          () -> CuriosConstants.LOG.error("{} is not a registered slot type!", id));
    }

    for (EntityType<?> entityType : toAdd) {
      map.computeIfAbsent(entityType, (k) -> new HashMap<>()).putAll(slots);
    }
    return map;
  }

  private static boolean readAndTestCondition(JsonObject json, ICondition.IContext context) {

    if (!json.has(ICondition.DEFAULT_FIELD)) {
      return true;
    }

    ICondition condition = Util.getOrThrow(ICondition.SAFE_CODEC.parse(JsonOps.INSTANCE,
        json.getAsJsonObject(ICondition.DEFAULT_FIELD)), JsonParseException::new);
    return condition.test(context);
  }

  public boolean hasSlots(EntityType<?> type) {
    return this.client.containsKey(type);
  }

  public Map<String, Integer> getClientSlots(EntityType<?> type) {

    if (this.client.containsKey(type)) {
      return this.client.get(type);
    }
    return ImmutableMap.of();
  }

  public Map<String, ISlotType> getEntitySlots(EntityType<?> type) {

    if (this.server.containsKey(type)) {
      return this.server.get(type);
    }
    return ImmutableMap.of();
  }

  public Map<String, Set<String>> getModsFromSlots() {
    return ImmutableMap.copyOf(idToMods);
  }
}
