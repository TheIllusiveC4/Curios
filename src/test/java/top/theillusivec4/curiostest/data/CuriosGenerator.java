package top.theillusivec4.curiostest.data;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosTriggers;
import top.theillusivec4.curios.api.SlotPredicate;

public class CuriosGenerator extends AdvancementProvider {

  public CuriosGenerator(DataGenerator generatorIn,
                         ExistingFileHelper fileHelperIn) {
    super(generatorIn, fileHelperIn);
  }

  @Override
  protected void registerAdvancements(@Nonnull Consumer<Advancement> consumer,
                                      @Nonnull ExistingFileHelper fileHelper) {
    Advancement.Builder.advancement()
        .addCriterion("test",
            CuriosTriggers.equip()
                .withItem(ItemPredicate.Builder.item()
                    .of(Items.DIAMOND))
                .withLocation(LocationPredicate.Builder.location()
                    .setBiome(Biomes.BADLANDS))
                .withSlot(SlotPredicate.Builder.slot()
                    .of("ring", "necklace")
                    .withIndex(MinMaxBounds.Ints.between(0, 10)))
                .build())
        .save(consumer, new ResourceLocation("curiostest", "test"), fileHelper);
  }
}
