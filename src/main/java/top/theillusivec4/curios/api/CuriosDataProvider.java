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
import net.minecraftforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.type.data.IEntitiesData;
import top.theillusivec4.curios.api.type.data.ISlotData;

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
        Path path = this.slotsPathProvider.json(new ResourceLocation(this.modId, slot));
        list.add(
            DataProvider.saveStable(pOutput, slotBuilder.serialize(), path));
      });
      this.entitiesBuilders.forEach((entities, entitiesBuilder) -> {
        Path path = this.entitiesPathProvider.json(new ResourceLocation(this.modId, entities));
        list.add(
            DataProvider.saveStable(pOutput, entitiesBuilder.serialize(), path));
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
