package top.theillusivec4.curios.common.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.lang3.EnumUtils;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.common.slottype.LegacySlotManager;
import top.theillusivec4.curios.common.slottype.SlotType;

public class CuriosSlotManager extends SimpleJsonResourceReloadListener {

  private static final Gson GSON =
      (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
  public static final CuriosSlotManager INSTANCE = new CuriosSlotManager();
  private Map<String, ISlotType> slots = ImmutableMap.of();
  private Map<String, Set<String>> idToMods = ImmutableMap.of();

  public CuriosSlotManager() {
    super(GSON, "curios/slots");
  }

  protected void apply(@Nonnull Map<ResourceLocation, JsonElement> pObject,
                       @Nonnull ResourceManager pResourceManager,
                       @Nonnull ProfilerFiller pProfiler) {
    Map<String, SlotType.Builder> map = new HashMap<>();
    Map<String, ImmutableSet.Builder<String>> modMap = new HashMap<>();

    for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
      ResourceLocation resourcelocation = entry.getKey();

      if (resourcelocation.getNamespace().equals("curios")) {

        try {
          String id = resourcelocation.getPath();
          fromJson(map.computeIfAbsent(id, (k) -> new SlotType.Builder(id)),
              GsonHelper.convertToJsonObject(entry.getValue(), "top element"));
          modMap.computeIfAbsent(id, (k) -> ImmutableSet.builder())
              .add(resourcelocation.getNamespace());
        } catch (IllegalArgumentException | JsonParseException e) {
          Curios.LOGGER.error("Parsing error loading curio slot {}", resourcelocation, e);
        }
      }
    }

    // Legacy IMC slot registrations
    for (Map.Entry<String, SlotType.Builder> entry : LegacySlotManager.getImcBuilders()
        .entrySet()) {
      SlotType.Builder builder =
          map.computeIfAbsent(entry.getKey(), (k) -> new SlotType.Builder(entry.getKey()));
      builder.apply(entry.getValue());
    }

    for (Map.Entry<String, Set<String>> entry : LegacySlotManager.getIdsToMods()
        .entrySet()) {
      modMap.computeIfAbsent(entry.getKey(), (k) -> ImmutableSet.builder())
          .addAll(entry.getValue());
    }

    for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
      ResourceLocation resourcelocation = entry.getKey();

      if (resourcelocation.getPath().startsWith("_") ||
          resourcelocation.getNamespace().equals("curios")) {
        continue;
      }

      try {
        String id = resourcelocation.getPath();
        fromJson(map.computeIfAbsent(id, (k) -> new SlotType.Builder(id)),
            GsonHelper.convertToJsonObject(entry.getValue(), "top element"));
        modMap.computeIfAbsent(id, (k) -> ImmutableSet.builder())
            .add(resourcelocation.getNamespace());
      } catch (IllegalArgumentException | JsonParseException e) {
        Curios.LOGGER.error("Parsing error loading curio slot {}", resourcelocation, e);
      }
    }
    this.slots = map.entrySet().stream()
        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> entry.getValue().build()));
    this.idToMods = modMap.entrySet().stream()
        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> entry.getValue().build()));
    Curios.LOGGER.info("Loaded {} curio slots", map.size());
  }

  public Map<String, ISlotType> getSlots() {
    return this.slots;
  }

  public Optional<ISlotType> getSlot(String id) {
    return Optional.ofNullable(this.slots.get(id));
  }

  public Map<String, Set<String>> getModsFromSlots() {
    return this.idToMods;
  }

  public static void fromJson(SlotType.Builder builder, JsonObject jsonObject)
      throws IllegalArgumentException, JsonParseException {
    Integer jsonSize = jsonObject.has("size") ? GsonHelper.getAsInt(jsonObject, "size") : null;

    if (jsonSize != null && jsonSize < 0) {
      throw new IllegalArgumentException("Size cannot be less than 0!");
    }
    String operation = GsonHelper.getAsString(jsonObject, "operation", "SET");

    if (!operation.equals("SET") && !operation.equals("ADD") && !operation.equals("REMOVE")) {
      throw new IllegalArgumentException(operation + " is not a valid operation!");
    }
    String jsonDropRule = GsonHelper.getAsString(jsonObject, "drop_rule", "");

    if (!jsonDropRule.isEmpty() && !EnumUtils.isValidEnum(ICurio.DropRule.class, jsonDropRule)) {
      throw new IllegalArgumentException(jsonDropRule + " is not a valid drop rule!");
    }
    boolean replace = GsonHelper.getAsBoolean(jsonObject, "replace", false);
    Integer jsonOrder = jsonObject.has("order") ? GsonHelper.getAsInt(jsonObject, "order") : null;
    String jsonIcon = GsonHelper.getAsString(jsonObject, "icon", "");
    boolean jsonToggle = GsonHelper.getAsBoolean(jsonObject, "render_toggle", true);
    boolean jsonCosmetic = GsonHelper.getAsBoolean(jsonObject, "add_cosmetic", false);
    boolean jsonNative = GsonHelper.getAsBoolean(jsonObject, "use_native_gui", true);

    if (jsonOrder != null) {
      builder.order(jsonOrder, replace);
    }

    if (!jsonIcon.isEmpty()) {
      builder.icon(new ResourceLocation(jsonIcon));
    }

    if (!jsonDropRule.isEmpty()) {
      builder.dropRule(jsonDropRule);
    }

    if (jsonSize != null) {
      builder.size(jsonSize, operation, replace);
    }
    builder.hasCosmetic(jsonCosmetic, replace);
    builder.useNativeGui(jsonNative, replace);
    builder.renderToggle(jsonToggle, replace);
  }
}
