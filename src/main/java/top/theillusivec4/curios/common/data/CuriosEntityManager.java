package top.theillusivec4.curios.common.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.common.slottype.LegacySlotManager;

public class CuriosEntityManager extends SimpleJsonResourceReloadListener {

  private static final Gson GSON =
      (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

  public static final CuriosEntityManager INSTANCE = new CuriosEntityManager();
  private Map<EntityType<?>, Map<String, ISlotType>> server = ImmutableMap.of();
  private Map<EntityType<?>, Set<String>> client = ImmutableMap.of();

  public CuriosEntityManager() {
    super(GSON, "curios/entities");
  }

  protected void apply(Map<ResourceLocation, JsonElement> pObject,
                       @Nonnull ResourceManager pResourceManager,
                       @Nonnull ProfilerFiller pProfiler) {
    Map<EntityType<?>, ImmutableMap.Builder<String, ISlotType>> map = new HashMap<>();

    for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
      ResourceLocation resourcelocation = entry.getKey();

      if (resourcelocation.getPath().startsWith("_")) {
        continue;
      }

      try {
        for (Map.Entry<EntityType<?>, Map<String, ISlotType>> entry1 : getSlotsForEntities(
            GsonHelper.convertToJsonObject(entry.getValue(), "top element")).entrySet()) {
          map.computeIfAbsent(entry1.getKey(), (k) -> ImmutableMap.builder())
              .putAll(entry1.getValue());
        }
      } catch (IllegalArgumentException | JsonParseException e) {
        Curios.LOGGER.error("Parsing error loading curio entity {}", resourcelocation, e);
      }
    }

    // Legacy IMC slot registrations - players only
    for (String s : LegacySlotManager.getImcBuilders().keySet()) {
      ImmutableMap.Builder<String, ISlotType> builder =
          map.computeIfAbsent(EntityType.PLAYER, (k) -> ImmutableMap.builder());
      CuriosSlotManager.INSTANCE.getSlot(s).ifPresentOrElse(slot -> builder.put(s, slot),
          () -> Curios.LOGGER.error("{} is not a registered slot type!", s));
    }

    this.server = map.entrySet().stream().collect(
        ImmutableMap.toImmutableMap(Map.Entry::getKey, (entry) -> entry.getValue().build()));
    Curios.LOGGER.info("Loaded {} curio entities", map.size());
  }

  public static ListTag getSyncPacket() {
    ListTag tag = new ListTag();

    for (Map.Entry<EntityType<?>, Map<String, ISlotType>> entry : INSTANCE.server.entrySet()) {
      ResourceLocation rl = ForgeRegistries.ENTITY_TYPES.getKey(entry.getKey());

      if (rl != null) {
        CompoundTag entity = new CompoundTag();
        entity.putString("Entity", rl.toString());
        ListTag list = new ListTag();

        for (String s : entry.getValue().keySet()) {
          list.add(StringTag.valueOf(s));
        }
        entity.put("Slots", list);
        tag.add(entity);
      }
    }
    return tag;
  }

  public static void applySyncPacket(ListTag tag) {
    Map<EntityType<?>, ImmutableSet.Builder<String>> map = new HashMap<>();

    for (Tag tag1 : tag) {

      if (tag1 instanceof CompoundTag entity) {
        EntityType<?> type =
            ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entity.getString("Entity")));

        if (type != null) {
          ListTag list = entity.getList("Slots", Tag.TAG_STRING);

          for (Tag tag2 : list) {

            if (tag2 instanceof StringTag slot) {
              map.computeIfAbsent(type, (k) -> ImmutableSet.builder()).add(slot.getAsString());
            }
          }
        }
      }
    }
    INSTANCE.client = map.entrySet().stream().collect(
        ImmutableMap.toImmutableMap(Map.Entry::getKey, (entry) -> entry.getValue().build()));
  }

  private static Map<EntityType<?>, Map<String, ISlotType>> getSlotsForEntities(
      JsonObject jsonObject) {
    Map<EntityType<?>, Map<String, ISlotType>> map = new HashMap<>();
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
          Curios.LOGGER.error("{} is not a registered entity type!", entity);
        }
      }
    }
    JsonArray jsonSlots = GsonHelper.getAsJsonArray(jsonObject, "slots", new JsonArray());
    Map<String, ISlotType> slots = new HashMap<>();

    for (JsonElement jsonSlot : jsonSlots) {
      String id = jsonSlot.getAsString();
      CuriosSlotManager.INSTANCE.getSlot(id).ifPresentOrElse(slot -> slots.put(id, slot),
          () -> Curios.LOGGER.error("{} is not a registered slot type!", id));
    }

    for (EntityType<?> entityType : toAdd) {
      map.computeIfAbsent(entityType, (k) -> new HashMap<>()).putAll(slots);
    }
    return map;
  }

  public boolean hasSlots(EntityType<?> type) {
    return this.client.containsKey(type);
  }

  public Map<String, ISlotType> getEntitySlots(EntityType<?> type) {

    if (this.server.containsKey(type)) {
      return this.server.get(type);
    }
    return ImmutableMap.of();
  }
}
