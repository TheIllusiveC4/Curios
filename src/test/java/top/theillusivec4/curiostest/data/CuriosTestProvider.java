package top.theillusivec4.curiostest.data;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.crafting.conditions.FalseCondition;
import net.minecraftforge.common.crafting.conditions.TrueCondition;
import net.minecraftforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.data.ISlotData;

public class CuriosTestProvider extends CuriosDataProvider {

  public CuriosTestProvider(
      String modId,
      PackOutput output,
      ExistingFileHelper fileHelper,
      CompletableFuture<HolderLookup.Provider> registries) {
    super(modId, output, fileHelper, registries);
  }

  @Override
  public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
    createSlot("test_slot")
        .size(4)
        .dropRule(ICurio.DropRule.ALWAYS_KEEP)
        .operation("ADD")
        .addCosmetic(true)
        .addCondition(FalseCondition.INSTANCE);

    createEntities("test_entities")
        .addPlayer()
        .addEntities(EntityType.ZOMBIE)
        .addSlots("head", "ring", "necklace")
        .addCondition(TrueCondition.INSTANCE);
  }
}
