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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.type.data.ISlotData;

public class SlotData implements ISlotData {

  private final String id;
  private final boolean includeId;

  private Integer order;
  private Integer size;
  private String operation;
  private Boolean useNativeGui;
  private Boolean hasCosmetic;
  private ResourceLocation icon;
  private DropRule dropRule;
  private Boolean renderToggle;
  private Boolean replace;
  private List<ICondition> conditions;
  private List<ResourceLocation> validators;
  private List<Either<TagKey<EntityType<?>>, ResourceKey<EntityType<?>>>> entities;

  public SlotData(String id, boolean includeId) {
    this.id = id;
    this.includeId = includeId;
  }

  @Override
  public ISlotData replace(boolean replace) {
    this.replace = replace;
    return this;
  }

  @Override
  public ISlotData order(int order) {
    this.order = order;
    return this;
  }

  @Override
  public ISlotData size(int size) {
    this.size = size;
    return this;
  }

  @Override
  public ISlotData operation(String operation) {
    this.operation = operation.toLowerCase();
    return this;
  }

  @Override
  public ISlotData useNativeGui(boolean useNativeGui) {
    this.useNativeGui = useNativeGui;
    return this;
  }

  @Override
  public ISlotData addCosmetic(boolean addCosmetic) {
    this.hasCosmetic = addCosmetic;
    return this;
  }

  @Override
  public ISlotData renderToggle(boolean renderToggle) {
    this.renderToggle = renderToggle;
    return this;
  }

  @Override
  public ISlotData icon(ResourceLocation icon) {
    this.icon = icon;
    return this;
  }

  @Override
  public ISlotData dropRule(DropRule dropRule) {
    this.dropRule = dropRule;
    return this;
  }

  @Override
  public ISlotData addCondition(ICondition... conditions) {

    if (this.conditions == null) {
      this.conditions = new ArrayList<>();
    }
    this.conditions.addAll(Arrays.asList(conditions));
    return this;
  }

  @Override
  public ISlotData addValidator(ResourceLocation... resourceLocation) {

    if (this.validators == null) {
      this.validators = new ArrayList<>();
    }
    this.validators.addAll(Arrays.asList(resourceLocation));
    return this;
  }

  @Override
  public ISlotData addEntity(EntityType<?>... entityTypes) {

    if (this.entities == null) {
      this.entities = new ArrayList<>();
    }

    for (EntityType<?> entityType : entityTypes) {
      BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityType).ifPresent(val -> {
        this.entities.add(Either.right(val));
      });
    }
    return this;
  }

  @Override
  public ISlotData addEntities(TagKey<EntityType<?>>... tagKeys) {

    if (this.entities == null) {
      this.entities = new ArrayList<>();
    }

    for (TagKey<EntityType<?>> tagKey : tagKeys) {
      this.entities.add(Either.left(tagKey));
    }
    return this;
  }

  @Override
  public String getId() {
    return this.id;
  }

  public ISlotData.Entry build() {
    return new SlotData.Entry(this.replace != null ? this.replace : false,
                              this.includeId ? Optional.of(this.id) : Optional.empty(),
                              Optional.ofNullable(this.order),
                              Optional.ofNullable(this.size),
                              Optional.ofNullable(this.operation),
                              Optional.ofNullable(this.useNativeGui),
                              Optional.ofNullable(this.hasCosmetic),
                              Optional.ofNullable(this.icon),
                              Optional.ofNullable(this.dropRule),
                              Optional.ofNullable(this.renderToggle),
                              this.conditions != null ? this.conditions : List.of(),
                              Optional.ofNullable(this.validators),
                              Optional.ofNullable(this.entities));
  }

  @Override
  public JsonObject serialize(HolderLookup.Provider provider) {
    JsonObject jsonObject = new JsonObject();

    if (this.replace != null) {
      jsonObject.addProperty("replace", this.replace);
    }

    if (this.order != null) {
      jsonObject.addProperty("order", this.order);
    }

    if (this.size != null) {
      jsonObject.addProperty("size", this.size);
    }

    if (this.operation != null) {
      jsonObject.addProperty("operation", this.operation);
    }

    if (this.useNativeGui != null) {
      jsonObject.addProperty("use_native_gui", this.useNativeGui);
    }

    if (this.hasCosmetic != null) {
      jsonObject.addProperty("add_cosmetic", this.hasCosmetic);
    }

    if (this.icon != null) {
      jsonObject.addProperty("icon", this.icon.toString());
    }

    if (this.dropRule != null) {
      jsonObject.addProperty("drop_rule", this.dropRule.toString());
    }

    if (this.renderToggle != null) {
      jsonObject.addProperty("render_toggle", this.renderToggle);
    }

    if (this.conditions != null) {
      ICondition.writeConditions(provider, jsonObject, this.conditions);
    }

    if (this.validators != null) {
      JsonArray arr = new JsonArray();

      for (ResourceLocation slotResultPredicate : this.validators) {
        arr.add(slotResultPredicate.toString());
      }
      jsonObject.add("validators", arr);
    }
    return jsonObject;
  }

  public record Entry(boolean replace,
                      Optional<String> id,
                      Optional<Integer> order,
                      Optional<Integer> size,
                      Optional<String> operation,
                      Optional<Boolean> useNativeGui,
                      Optional<Boolean> hasCosmetic,
                      Optional<ResourceLocation> icon,
                      Optional<DropRule> dropRule,
                      Optional<Boolean> renderToggle,
                      List<ICondition> conditions,
                      Optional<List<ResourceLocation>> validators,
                      Optional<List<Either<TagKey<EntityType<?>>, ResourceKey<EntityType<?>>>>> entities)
      implements ISlotData.Entry {


  }
}
