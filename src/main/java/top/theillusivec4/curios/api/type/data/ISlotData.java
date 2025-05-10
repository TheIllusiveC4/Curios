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

package top.theillusivec4.curios.api.type.data;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.ApiStatus;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.internal.CuriosServices;

/**
 * Used in data generation to represent the slot data
 */
public interface ISlotData {

  ISlotData replace(boolean replace);

  ISlotData order(int order);

  ISlotData size(int size);

  ISlotData operation(String operation);

  ISlotData useNativeGui(boolean useNativeGui);

  ISlotData addCosmetic(boolean addCosmetic);

  ISlotData renderToggle(boolean renderToggle);

  ISlotData icon(ResourceLocation icon);

  ISlotData dropRule(DropRule dropRule);

  ISlotData addCondition(ICondition... condition);

  ISlotData addValidator(ResourceLocation... resourceLocation);

  ISlotData addEntity(EntityType<?>... entityTypes);

  ISlotData addEntities(TagKey<EntityType<?>>... tagKey);

  @ApiStatus.Internal
  String getId();

  @ApiStatus.Internal
  ISlotData.Entry build();

  @Deprecated(forRemoval = true)
  JsonObject serialize(HolderLookup.Provider provider);

  interface Entry {

    Codec<ISlotData.Entry> CODEC = CuriosServices.CODECS.slotDataEntryCodec();

    boolean replace();

    Optional<String> id();

    Optional<Integer> order();

    Optional<Integer> size();

    Optional<String> operation();

    Optional<Boolean> useNativeGui();

    Optional<Boolean> hasCosmetic();

    Optional<ResourceLocation> icon();

    Optional<DropRule> dropRule();

    Optional<Boolean> renderToggle();

    List<ICondition> conditions();

    Optional<List<ResourceLocation>> validators();

    Optional<List<Either<TagKey<EntityType<?>>, ResourceKey<EntityType<?>>>>> entities();
  }
}
