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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.apache.commons.lang3.EnumUtils;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.common.slottype.LegacySlotManager;
import top.theillusivec4.curios.common.slottype.SlotType;

public class CuriosSlotManager extends SimpleJsonResourceReloadListener {

  private static final Gson GSON =
      (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
  public static CuriosSlotManager INSTANCE = new CuriosSlotManager();
  private Map<String, ISlotType> slots = ImmutableMap.of();
  private Map<String, ResourceLocation> icons = ImmutableMap.of();
  private Map<String, Set<String>> idToMods = ImmutableMap.of();
  private ICondition.IContext ctx = ICondition.IContext.EMPTY;

  public CuriosSlotManager() {
    super(GSON, "curios/slots");
  }

  public CuriosSlotManager(ICondition.IContext ctx) {
    super(GSON, "curios/slots");
    this.ctx = ctx;
  }

  protected void apply(@Nonnull Map<ResourceLocation, JsonElement> pObject,
                       @Nonnull ResourceManager pResourceManager,
                       @Nonnull ProfilerFiller pProfiler) {
    Map<String, SlotType.Builder> map = new HashMap<>();
    Map<String, ImmutableSet.Builder<String>> modMap = new HashMap<>();
    Map<ResourceLocation, JsonElement> sorted = new LinkedHashMap<>();
    pResourceManager.listPacks().forEach(packResources -> {
      Set<String> namespaces = packResources.getNamespaces(PackType.SERVER_DATA);
      namespaces.forEach(
          namespace -> packResources.listResources(PackType.SERVER_DATA, namespace, "curios/slots",
              (resourceLocation, inputStreamIoSupplier) -> {
                String path = resourceLocation.getPath();
                ResourceLocation rl = new ResourceLocation(namespace,
                    path.substring("curios/slots/".length(), path.length() - ".json".length()));

                JsonElement el = pObject.get(rl);
                if (el != null) {
                  sorted.put(rl, el);
                }
              }));
    });

    for (Map.Entry<ResourceLocation, JsonElement> entry : sorted.entrySet()) {
      ResourceLocation resourcelocation = entry.getKey();

      if (resourcelocation.getNamespace().equals("curios")) {

        try {
          String id = resourcelocation.getPath();

          if (!readAndTestCondition(entry.getValue().getAsJsonObject(), this.ctx)) {
            Curios.LOGGER.debug("Skipping loading slot {} as its conditions were not met",
                resourcelocation);
            continue;
          }
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

    for (Map.Entry<ResourceLocation, JsonElement> entry : sorted.entrySet()) {
      ResourceLocation resourcelocation = entry.getKey();

      if (resourcelocation.getPath().startsWith("_") ||
          resourcelocation.getNamespace().equals("curios")) {
        continue;
      }

      try {
        String id = resourcelocation.getPath();

        if (!readAndTestCondition(entry.getValue().getAsJsonObject(), this.ctx)) {
          Curios.LOGGER.debug("Skipping loading slot {} as its conditions were not met",
              resourcelocation);
          continue;
        }
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

  private static boolean readAndTestCondition(JsonObject json, ICondition.IContext context) {

    if (!json.has(ICondition.DEFAULT_FIELD)) {
      return true;
    }

    ICondition condition = Util.getOrThrow(ICondition.SAFE_CODEC.parse(JsonOps.INSTANCE,
        json.getAsJsonObject(ICondition.DEFAULT_FIELD)), JsonParseException::new);
    return condition.test(context);
  }

  public Map<String, ISlotType> getSlots() {
    return this.slots;
  }

  public Optional<ISlotType> getSlot(String id) {
    return Optional.ofNullable(this.slots.get(id));
  }

  public void setIcons(Map<String, ResourceLocation> icons) {
    this.icons = ImmutableMap.copyOf(icons);
  }

  public Map<String, ResourceLocation> getIcons() {
    return this.icons;
  }

  public ResourceLocation getIcon(String identifier) {
    return this.icons.getOrDefault(identifier,
        new ResourceLocation(CuriosApi.MODID, "slot/empty_curio_slot"));
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
    Boolean jsonToggle =
        jsonObject.has("render_toggle") ? GsonHelper.getAsBoolean(jsonObject, "render_toggle") :
            null;
    Boolean jsonCosmetic =
        jsonObject.has("add_cosmetic") ? GsonHelper.getAsBoolean(jsonObject, "add_cosmetic") : null;
    Boolean jsonNative =
        jsonObject.has("use_native_gui") ? GsonHelper.getAsBoolean(jsonObject, "use_native_gui") :
            null;

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

    if (jsonCosmetic != null) {
      builder.hasCosmetic(jsonCosmetic, replace);
    }

    if (jsonNative != null) {
      builder.useNativeGui(jsonNative, replace);
    }

    if (jsonToggle != null) {
      builder.renderToggle(jsonToggle, replace);
    }
  }
}
