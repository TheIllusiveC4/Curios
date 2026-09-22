package top.theillusivec4.curiostest.data;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import top.theillusivec4.curios.api.CuriosTriggers;
import top.theillusivec4.curios.api.SlotPredicate;
import top.theillusivec4.curiostest.CuriosTest;

public class CuriosGenerator extends AdvancementSubProvider {

  public CuriosGenerator(BootstrapContext<Advancement> output) {
    super(output);
  }

  @Override
  public void generate() {
    Advancement.Builder.advancement()
        .addCriterion("test",
            CuriosTriggers.equip()
                .withItem(ItemPredicate.Builder.item()
                    .of(BuiltInRegistries.ITEM, Items.DIAMOND))
                .withLocation(LocationPredicate.Builder.location()
                    .setBiomes(HolderSet.direct(
                        this.output.lookup(Registries.BIOME)
                            .getOrThrow(Biomes.BADLANDS))))
                .withSlot(SlotPredicate.Builder.slot()
                    .of("ring", "necklace")
                    .withIndex(MinMaxBounds.Ints.between(0, 10)))
                .build())
        .save(this.output, Identifier.fromNamespaceAndPath(CuriosTest.MODID, "test"));
  }
}
