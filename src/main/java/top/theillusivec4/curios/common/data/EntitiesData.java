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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.conditions.ICondition;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.CuriosTags;
import top.theillusivec4.curios.api.type.data.IEntitiesData;
import top.theillusivec4.curios.api.type.data.ISlotData;

public class EntitiesData implements IEntitiesData {

  private final List<Either<TagKey<EntityType<?>>, ResourceKey<EntityType<?>>>> entities =
      new ArrayList<>();
  private final List<Either<String, IEntitySlotEntry>> slots = new ArrayList<>();
  private Boolean replace;
  private List<ICondition> conditions;

  @Override
  public IEntitiesData replace(boolean replace) {
    this.replace = replace;
    return this;
  }

  @Override
  public IEntitiesData addPlayer() {
    return addEntities(CuriosTags.PLAYER_LIKE);
  }

  @Override
  public IEntitiesData addEntities(EntityType<?>... entityTypes) {

    for (EntityType<?> entityType : entityTypes) {
      BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityType).ifPresent(val -> {
        this.entities.add(Either.right(val));
      });
    }
    return this;
  }

  @SafeVarargs
  @Override
  public final IEntitiesData addEntities(TagKey<EntityType<?>>... entityTag) {

    for (TagKey<EntityType<?>> tagKey : entityTag) {
      this.entities.add(Either.left(tagKey));
    }
    return this;
  }

  @Override
  public IEntitiesData addAllPresetSlots() {
    return this.addSlots(Arrays.stream(CuriosSlotTypes.Preset.values())
                             .map(CuriosSlotTypes.Preset::id)
                             .toArray(String[]::new));
  }

  @Override
  public IEntitiesData addAllPresetSlotsExcept(CuriosSlotTypes.Preset... presets) {
    return this.addSlots(Arrays.stream(CuriosSlotTypes.Preset.values())
                             .filter(preset -> Arrays.stream(presets).noneMatch(preset::equals))
                             .map(CuriosSlotTypes.Preset::id)
                             .toArray(String[]::new));
  }

  @Override
  public IEntitiesData addPresetSlots(CuriosSlotTypes.Preset... preset) {
    return this.addSlots(Arrays.stream(preset)
                             .map(CuriosSlotTypes.Preset::id)
                             .toArray(String[]::new));
  }

  @Override
  public IEntitiesData addSlots(String... slots) {
    for (String slot : slots) {
      this.slots.add(Either.left(slot));
    }
    return this;
  }

  @Override
  public IEntitiesData addSlots(boolean create, ISlotData... slotData) {
    for (ISlotData slot : slotData) {
      ISlotData.Entry old = slot.build();
      ISlotData.Entry copy = new SlotData.Entry(old.replace(),
                         Optional.ofNullable(slot.getId()),
                         old.order(),
                         old.size(),
                         old.operation(),
                         old.useNativeGui(),
                         old.hasCosmetic(),
                         old.icon(),
                         old.dropRule(),
                         old.renderToggle(),
                         old.conditions(),
                         old.validators(),
                         old.entities());
      this.slots.add(Either.right(new EntitySlotEntry(create, copy)));
    }
    return this;
  }

  @Override
  public IEntitiesData addSlots(ISlotData... slotData) {
    return this.addSlots(true, slotData);
  }

  @Override
  public IEntitiesData addCondition(ICondition condition) {

    if (this.conditions == null) {
      this.conditions = new ArrayList<>();
    }
    this.conditions.add(condition);
    return this;
  }

  @Override
  public Entry build() {
    return new EntitiesData.Entry(this.replace != null ? this.replace : false, this.entities,
                                  this.slots,
                                  this.conditions != null ? this.conditions : List.of());
  }

  @Override
  public JsonObject serialize(HolderLookup.Provider provider) {
    JsonObject jsonObject = new JsonObject();

    if (this.replace != null) {
      jsonObject.addProperty("replace", this.replace);
    }

    if (!this.entities.isEmpty()) {
      JsonArray arr = new JsonArray();
      this.entities.forEach(entityType -> {
        entityType.ifLeft(entity -> {
          arr.add("#" + entity.location());
        });
        entityType.ifRight(entity -> {
          arr.add(entity.location().toString());
        });
      });
      jsonObject.add("entities", arr);
    }

    if (!this.slots.isEmpty()) {
      JsonArray arr = new JsonArray();
      for (Either<String, IEntitySlotEntry> slot : this.slots) {
        arr.add((String) slot.map(str -> str, sl -> sl.slot().id().orElseThrow()));
      }
      jsonObject.add("slots", arr);
    }

    if (this.conditions != null) {
      ICondition.writeConditions(provider, jsonObject, this.conditions);
    }
    return jsonObject;
  }

  public record Entry(boolean replace,
                      List<Either<TagKey<EntityType<?>>, ResourceKey<EntityType<?>>>> entities,
                      List<Either<String, IEntitySlotEntry>> slots,
                      List<ICondition> conditions) implements IEntitiesData.Entry {

  }

  public record EntitySlotEntry(boolean create, ISlotData.Entry slot) implements IEntitySlotEntry {

    public static final Codec<IEntitySlotEntry> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.BOOL
                .optionalFieldOf("create", true)
                .forGetter(IEntitySlotEntry::create),
            ISlotData.Entry.CODEC
                .fieldOf("slot")
                .forGetter(IEntitySlotEntry::slot)
        ).apply(instance, EntitySlotEntry::new)
    );
  }
}
