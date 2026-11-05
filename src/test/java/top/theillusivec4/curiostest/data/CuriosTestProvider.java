package top.theillusivec4.curiostest.data;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.references.ItemIds;
import net.minecraft.world.entity.EntityTypes;
import net.neoforged.neoforge.common.conditions.NeoForgeConditions;
import top.theillusivec4.curios.api.CuriosDataProvider;
import top.theillusivec4.curios.api.CuriosTags;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.type.data.ISlotData;
import top.theillusivec4.curiostest.common.CuriosTestIds;

public class CuriosTestProvider extends CuriosDataProvider {

  public CuriosTestProvider(
      String modId,
      PackOutput output,
      CompletableFuture<HolderLookup.Provider> registries) {
    super(modId, output, registries);
  }

  @Override
  public void generate(HolderLookup.Provider registries) {
    this.createSlot("never_slot")
        .size(4)
        .dropRule(DropRule.ALWAYS_KEEP)
        .operation("ADD")
        .addCosmetic(true)
        .addCondition(NeoForgeConditions.never());

    ISlotData testSlot = this.createSlot("slot_from_slots").size(2);
    this.tag(testSlot).add(ItemIds.DIAMOND);

    this.tag(CuriosTags.HANDS).add(CuriosTestIds.KNUCKLES);
    this.tag(CuriosTags.NECKLACE).add(CuriosTestIds.AMULET);
    this.tag(CuriosTags.RING).add(CuriosTestIds.RING);
    this.tag(CuriosTags.HEAD).add(CuriosTestIds.CROWN);

    this.createEntities("test_entities")
        .addPlayer()
        .addEntities(EntityTypes.WITHER_SKELETON)
        .addAllPresetSlots()
        .addSlots("inline_from_entities")
        .addSlots(this.getSlot("slot_from_entities").size(3))
        .addCondition(NeoForgeConditions.always());
  }
}
