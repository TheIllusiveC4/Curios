package top.theillusivec4.curiostest.data;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosTriggers;

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
            CuriosTriggers.equipAtLocation(
                ItemPredicate.Builder.item().of(Items.DIAMOND),
                LocationPredicate.Builder.location().setBiome(Biomes.BADLANDS)))
        .save(consumer, new ResourceLocation("curiostest", "test"), fileHelper);
  }
}
