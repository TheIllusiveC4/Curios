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

import java.util.*;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.conditions.ICondition;
import top.theillusivec4.curios.api.type.data.IEntitiesData;

public class EntitiesData implements IEntitiesData {

  private final Set<EntityType<?>> entities = new LinkedHashSet<>();
  private final Set<String> slots = new LinkedHashSet<>();
  private Boolean replace;
  private List<ICondition> conditions;

  @Override
  public EntitiesData replace(boolean replace) {
    this.replace = replace;
    return this;
  }

  @Override
  public EntitiesData addPlayer() {
    return addEntities(EntityType.PLAYER);
  }

  @Override
  public EntitiesData addEntities(EntityType<?>... entityTypes) {
    this.entities.addAll(Arrays.stream(entityTypes).toList());
    return this;
  }

  @Override
  public EntitiesData addSlots(String... slots) {
    this.slots.addAll(Arrays.stream(slots).toList());
    return this;
  }

  @Override
  public EntitiesData addCondition(ICondition condition) {

    if (this.conditions == null) {
      this.conditions = new ArrayList<>();
    }
    this.conditions.add(condition);
    return this;
  }

  @Override
  public JsonObject serialize(HolderLookup.Provider provider) {
    JsonObject jsonObject = new JsonObject();

    if (this.replace != null) {
      jsonObject.addProperty("replace", this.replace);
    }

    if (!this.entities.isEmpty()) {
      JsonArray arr = new JsonArray();
      this.entities.forEach(entityType -> arr.add(
          Objects.requireNonNull(BuiltInRegistries.ENTITY_TYPE.getKey(entityType)).toString()));
      jsonObject.add("entities", arr);
    }

    if (!this.slots.isEmpty()) {
      JsonArray arr = new JsonArray();
      this.slots.forEach(arr::add);
      jsonObject.add("slots", arr);
    }

    if (this.conditions != null) {
      ICondition.writeConditions(provider, jsonObject, this.conditions);
    }
    return jsonObject;
  }
}
