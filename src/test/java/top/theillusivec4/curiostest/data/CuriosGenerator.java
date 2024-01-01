package top.theillusivec4.curiostest.data;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import top.theillusivec4.curios.api.CuriosTriggers;

public class CuriosGenerator implements ForgeAdvancementProvider.AdvancementGenerator {

  @Override
  public void generate(@Nonnull HolderLookup.Provider registries,
                       @Nonnull Consumer<Advancement> saver,
                       @Nonnull ExistingFileHelper existingFileHelper) {
    Advancement.Builder.advancement()
        .addCriterion("test",
            CuriosTriggers.equipAtLocation(
                ItemPredicate.Builder.item().of(Items.DIAMOND),
                LocationPredicate.Builder.location().setBiome(Biomes.BADLANDS)))
        .save(saver, new ResourceLocation("curiostest", "test"), existingFileHelper);
  }
}
