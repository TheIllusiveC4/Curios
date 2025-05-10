package top.theillusivec4.curiostest.data;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import top.theillusivec4.curios.api.CuriosTriggers;
import top.theillusivec4.curios.api.SlotPredicate;

public class CuriosGenerator implements AdvancementSubProvider {


  @Override
  public void generate(@Nonnull HolderLookup.Provider registries,
                       @Nonnull Consumer<AdvancementHolder> saver) {
    Advancement.Builder.advancement()
        .addCriterion("test",
                      CuriosTriggers.equip()
                          .withItem(ItemPredicate.Builder.item()
                                        .of(BuiltInRegistries.ITEM, Items.DIAMOND))
                          .withLocation(LocationPredicate.Builder.location()
                                            .setBiomes(HolderSet.direct(
                                                registries.lookupOrThrow(Registries.BIOME)
                                                    .getOrThrow(Biomes.BADLANDS))))
                          .withSlot(SlotPredicate.Builder.slot()
                                        .of("ring", "necklace")
                                        .withIndex(MinMaxBounds.Ints.between(0, 10)))
                          .build())
        .save(saver, ResourceLocation.fromNamespaceAndPath("curiostest", "test"));
  }
}
