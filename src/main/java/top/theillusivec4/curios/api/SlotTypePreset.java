package top.theillusivec4.curios.api;

import java.util.Optional;
import net.minecraft.util.ResourceLocation;
import top.theillusivec4.curios.api.SlotTypeMessage.Builder;

/**
 * These slot type presets should be used whenever applicable by calling {@link
 * SlotTypePreset#getMessageBuilder()} and building off that in the IMC message
 */
public enum SlotTypePreset {
  HEAD("head", 40), NECKLACE("necklace", 60), BACK("back", 80), BODY("body", 100), BRACELET(
      "bracelet", 120), HANDS("hands", 140), RING("ring", 160), BELT("belt", 180), CHARM("charm",
      200);

  final String id;
  final int priority;

  SlotTypePreset(String id, int priority) {
    this.id = id;
    this.priority = priority;
  }

  /**
   * Attempts to find a valid preset for the given identifier and returns {@link Optional#empty()}
   * if none is found.
   *
   * @param id The {@link top.theillusivec4.curios.api.type.ISlotType} identifier
   * @return {@link Optional} wrapper for the found preset or {@link Optional#empty()}
   */
  public static Optional<SlotTypePreset> findPreset(String id) {
    try {
      return Optional.of(SlotTypePreset.valueOf(id.toUpperCase()));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public String getIdentifier() {
    return this.id;
  }

  public Builder getMessageBuilder() {
    return new Builder(this.id).priority(this.priority).icon(
        new ResourceLocation(CuriosApi.MODID, "item/empty_" + this.getIdentifier() + "_slot"));
  }
}
