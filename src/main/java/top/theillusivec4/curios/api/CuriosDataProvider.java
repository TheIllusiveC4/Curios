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

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import top.theillusivec4.curios.api.internal.CuriosServices;
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
  private final Map<String, ISlotData> slotBuilders = new ConcurrentHashMap<>();
  private final Map<String, IEntitiesData> entitiesBuilders = new ConcurrentHashMap<>();
  private final CuriosBlockTagsProvider blockTagsProvider;
  private final CuriosItemTagsProvider itemTagsProvider;

  public CuriosDataProvider(String modId, PackOutput output,
                            CompletableFuture<HolderLookup.Provider> registries) {
    this.modId = modId;
    this.entitiesPathProvider =
        output.createPathProvider(PackOutput.Target.DATA_PACK, "curios/entities");
    this.slotsPathProvider =
        output.createPathProvider(PackOutput.Target.DATA_PACK, "curios/slots");
    this.registries = registries;
    this.blockTagsProvider = new CuriosBlockTagsProvider(output, registries, modId);
    this.itemTagsProvider =
        new CuriosItemTagsProvider(output, registries, this.blockTagsProvider.contentsGetter(),
                                   modId);
  }

  public abstract void generate(HolderLookup.Provider registries);

  @Nonnull
  public CompletableFuture<?> run(@Nonnull CachedOutput cachedOutput) {
    return this.registries.thenCompose((provider) -> {
      List<CompletableFuture<?>> list = new ArrayList<>();
      this.generate(provider);
      final DynamicOps<JsonElement> dynamicOps =
          provider.createSerializationContext(JsonOps.INSTANCE);
      this.slotBuilders.forEach((slot, slotBuilder) -> {
        Path path =
            this.slotsPathProvider.json(ResourceLocation.fromNamespaceAndPath(this.modId, slot));
        list.add(CompletableFuture.supplyAsync(() -> {
          return ISlotData.Entry.CODEC.encodeStart(dynamicOps, slotBuilder.build()).getOrThrow(
              msg -> new RuntimeException("Failed to encode %s: %s".formatted(path, msg)));
        }).thenComposeAsync(encoded -> DataProvider.saveStable(cachedOutput, encoded, path)));
      });
      this.entitiesBuilders.forEach((entities, entitiesBuilder) -> {
        Path path = this.entitiesPathProvider.json(
            ResourceLocation.fromNamespaceAndPath(this.modId, entities));
        list.add(CompletableFuture.supplyAsync(() -> {
          return IEntitiesData.Entry.CODEC.encodeStart(dynamicOps, entitiesBuilder.build())
              .getOrThrow(
                  msg -> new RuntimeException("Failed to encode %s: %s".formatted(path, msg)));
        }).thenComposeAsync(encoded -> DataProvider.saveStable(cachedOutput, encoded, path)));
      });
      list.add(this.itemTagsProvider.run(cachedOutput));
      return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
    });
  }

  public final ISlotData createSlot(String id) {
    return this.slotBuilders.computeIfAbsent(id, (k) -> createSlotData(id));
  }

  public final ISlotData getSlot(String id) {
    return this.slotBuilders.getOrDefault(id, createSlotData(id));
  }

  public final ISlotData copySlot(String id, String copyId) {

    if (id.equals(copyId)) {
      return createSlot(id);
    }
    return this.slotBuilders
        .computeIfAbsent(id, (k) -> this.slotBuilders.getOrDefault(copyId, createSlotData(copyId)));
  }

  public final IEntitiesData createEntities(String id) {
    return this.entitiesBuilders.computeIfAbsent(id, (k) -> createEntitiesData());
  }

  public final IEntitiesData copyEntities(String id, String copyId) {

    if (id.equals(copyId)) {
      return createEntities(id);
    }
    return this.entitiesBuilders
        .computeIfAbsent(id,
                         (k) -> this.entitiesBuilders.getOrDefault(copyId, createEntitiesData()));
  }

  public final IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> tag(TagKey<Item> tagKey) {
    return this.itemTagsProvider.tag(tagKey);
  }

  public final IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> tag(String slotId) {
    return this.itemTagsProvider.tag(
        TagKey.create(Registries.ITEM, CuriosResources.resource(slotId)));
  }

  public final IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> tag(ISlotData slot) {
    return this.itemTagsProvider.tag(
        TagKey.create(Registries.ITEM, CuriosResources.resource(slot.getId())));
  }

  @Nonnull
  public final String getName() {
    return "Curios for " + this.modId;
  }

  private static ISlotData createSlotData(String id) {
    return CuriosServices.SLOTS.getSlotData(id);
  }

  private static IEntitiesData createEntitiesData() {
    return CuriosServices.SLOTS.getEntitiesData();
  }

  private static class CuriosBlockTagsProvider extends BlockTagsProvider {

    public CuriosBlockTagsProvider(PackOutput output,
                                   CompletableFuture<HolderLookup.Provider> lookupProvider,
                                   String modId) {
      super(output, lookupProvider, modId);
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider provider) {

    }
  }

  private static class CuriosItemTagsProvider extends ItemTagsProvider {

    CompletableFuture<HolderLookup.Provider> lookupProvider;

    public CuriosItemTagsProvider(PackOutput output,
                                  CompletableFuture<HolderLookup.Provider> lookupProvider,
                                  CompletableFuture<TagLookup<Block>> blockTags, String modid) {
      super(output, lookupProvider, blockTags, modid);
      this.lookupProvider = lookupProvider;
    }

    @Nonnull
    @Override
    protected IntrinsicTagAppender<Item> tag(@Nonnull TagKey<Item> tag) {
      return super.tag(tag);
    }

    @Nonnull
    @Override
    protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
      return this.lookupProvider;
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider provider) {

    }
  }
}
