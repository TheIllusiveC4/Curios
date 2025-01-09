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

package top.theillusivec4.curios.api;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.type.data.IEntitiesData;
import top.theillusivec4.curios.api.type.data.ISlotData;

/**
 * Basic data generator for curios slots and entities
 */
public abstract class CuriosDataProvider implements DataProvider {
  private final PackOutput.PathProvider entitiesPathProvider;
  private final PackOutput.PathProvider slotsPathProvider;
  private final CompletableFuture<HolderLookup.Provider> registries;
  private final String modId;
  private final Map<String, ISlotData> slotBuilders = new HashMap<>();
  private final Map<String, IEntitiesData> entitiesBuilders = new HashMap<>();
  private final ExistingFileHelper fileHelper;

  public CuriosDataProvider(String modId, PackOutput output, ExistingFileHelper fileHelper,
                            CompletableFuture<HolderLookup.Provider> registries) {
    this.modId = modId;
    this.fileHelper = fileHelper;
    this.entitiesPathProvider =
        output.createPathProvider(PackOutput.Target.DATA_PACK, "curios/entities");
    this.slotsPathProvider =
        output.createPathProvider(PackOutput.Target.DATA_PACK, "curios/slots");
    this.registries = registries;
  }

  public abstract void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper);

  @Nonnull
  public CompletableFuture<?> run(@Nonnull CachedOutput pOutput) {
    return this.registries.thenCompose((p_255484_) -> {
      List<CompletableFuture<?>> list = new ArrayList<>();
      this.generate(p_255484_, this.fileHelper);
      this.slotBuilders.forEach((slot, slotBuilder) -> {
        Path path =
            this.slotsPathProvider.json(ResourceLocation.fromNamespaceAndPath(this.modId, slot));
        list.add(DataProvider.saveStable(pOutput, slotBuilder.serialize(p_255484_), path));
      });
      this.entitiesBuilders.forEach((entities, entitiesBuilder) -> {
        Path path = this.entitiesPathProvider.json(
            ResourceLocation.fromNamespaceAndPath(this.modId, entities));
        list.add(DataProvider.saveStable(pOutput, entitiesBuilder.serialize(p_255484_), path));
      });
      return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
    });
  }

  public final ISlotData createSlot(String id) {
    return this.slotBuilders.computeIfAbsent(id, (k) -> createSlotData());
  }

  public final ISlotData copySlot(String id, String copyId) {

    if (id.equals(copyId)) {
      return createSlot(id);
    }
    return this.slotBuilders.computeIfAbsent(id,
        (k) -> this.slotBuilders.getOrDefault(copyId, createSlotData()));
  }

  public final IEntitiesData createEntities(String id) {
    return this.entitiesBuilders.computeIfAbsent(id, (k) -> createEntitiesData());
  }

  public final IEntitiesData copyEntities(String id, String copyId) {

    if (id.equals(copyId)) {
      return createEntities(id);
    }
    return this.entitiesBuilders.computeIfAbsent(id,
        (k) -> this.entitiesBuilders.getOrDefault(copyId, createEntitiesData()));
  }

  @Nonnull
  public final String getName() {
    return "Curios for " + this.modId;
  }

  private static ISlotData createSlotData() {
    CuriosApi.apiError();
    return null;
  }

  private static IEntitiesData createEntitiesData() {
    CuriosApi.apiError();
    return null;
  }
}
