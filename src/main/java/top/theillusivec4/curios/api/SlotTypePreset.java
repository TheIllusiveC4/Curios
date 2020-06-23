package top.theillusivec4.curios.api;

import net.minecraft.util.ResourceLocation;
import top.theillusivec4.curios.api.CurioImcMessage.Builder;

public enum SlotTypePreset {
  HEAD("head", 40), NECKLACE("necklace", 60), BACK("back", 80), BODY("body", 100), HANDS("hands",
      120), RING("ring", 140), BELT("belt", 160), CHARM("charm", 180);

  final String id;
  final int priority;

  SlotTypePreset(String id, int priority) {
    this.id = id;
    this.priority = priority;
  }

  public String getIdentifier() {
    return this.id;
  }

  public Builder getMessageBuilder() {
    return new Builder(this.id).priority(this.priority).icon(
        new ResourceLocation(CuriosApi.MODID, "item/empty_" + this.getIdentifier() + "_slot"));
  }
}
