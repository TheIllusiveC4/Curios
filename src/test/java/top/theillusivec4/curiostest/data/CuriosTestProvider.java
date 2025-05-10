package top.theillusivec4.curiostest.data;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.NeoForgeConditions;
import top.theillusivec4.curios.api.CuriosDataProvider;
import top.theillusivec4.curios.api.CuriosTags;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.type.data.ISlotData;
import top.theillusivec4.curiostest.common.CuriosTestRegistry;

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
    this.tag(testSlot).add(Items.DIAMOND);

    this.tag(CuriosTags.HANDS).add(CuriosTestRegistry.KNUCKLES.get());
    this.tag(CuriosTags.NECKLACE).add(CuriosTestRegistry.AMULET.get());
    this.tag(CuriosTags.RING).add(CuriosTestRegistry.RING.get());
    this.tag(CuriosTags.HEAD).add(CuriosTestRegistry.CROWN.get());

    this.createEntities("test_entities")
        .addPlayer()
        .addEntities(EntityType.WITHER_SKELETON)
        .addAllPresetSlots()
        .addSlots("inline_from_entities")
        .addSlots(this.getSlot("slot_from_entities").size(3))
        .addCondition(NeoForgeConditions.always());
  }
}
