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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.conditions.ICondition;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.data.ISlotData;

public class SlotData implements ISlotData {

  private Integer order;
  private Integer size;
  private String operation;
  private Boolean useNativeGui;
  private Boolean hasCosmetic;
  private ResourceLocation icon;
  private ICurio.DropRule dropRule;
  private Boolean renderToggle;
  private Boolean replace;
  private List<ICondition> conditions;
  private Set<ResourceLocation> validators;

  @Override
  public SlotData replace(boolean replace) {
    this.replace = replace;
    return this;
  }

  @Override
  public SlotData order(int order) {
    this.order = order;
    return this;
  }

  @Override
  public SlotData size(int size) {
    this.size = size;
    return this;
  }

  @Override
  public SlotData operation(String operation) {
    this.operation = operation;
    return this;
  }

  @Override
  public SlotData operation(AttributeModifier.Operation operation) {
    this.operation = operation == AttributeModifier.Operation.ADD_VALUE ? "ADD" : "SET";
    return this;
  }

  @Override
  public SlotData useNativeGui(boolean useNativeGui) {
    this.useNativeGui = useNativeGui;
    return this;
  }

  @Override
  public SlotData addCosmetic(boolean addCosmetic) {
    this.hasCosmetic = addCosmetic;
    return this;
  }

  @Override
  public SlotData renderToggle(boolean renderToggle) {
    this.renderToggle = renderToggle;
    return this;
  }

  @Override
  public SlotData icon(ResourceLocation icon) {
    this.icon = icon;
    return this;
  }

  @Override
  public SlotData dropRule(ICurio.DropRule dropRule) {
    this.dropRule = dropRule;
    return this;
  }

  @Override
  public SlotData addCondition(ICondition condition) {

    if (this.conditions == null) {
      this.conditions = new ArrayList<>();
    }
    this.conditions.add(condition);
    return this;
  }

  @Override
  public ISlotData addValidator(ResourceLocation resourceLocation) {

    if (this.validators == null) {
      this.validators = new LinkedHashSet<>();
    }
    this.validators.add(resourceLocation);
    return this;
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
}
