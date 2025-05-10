package top.theillusivec4.curios.api.common.slot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.type.ISlotType;

/**
 * Record containing data for filtering slot types.
 *
 * @param id                 A list of identifiers for slot types.
 * @param size               A range of sizes to compare against the default slot type size.
 * @param dropRule           The drop rule for the slot type(s).
 * @param hasCosmetic        Whether the slot type has cosmetics.
 * @param canToggleRendering Whether the slot type can toggle rendering.
 * @see ISlotType
 */
public record SlotTypePredicate(List<String> id,
                                MinMaxBounds.Ints size,
                                Optional<DropRule> dropRule,
                                Optional<Boolean> hasCosmetic,
                                Optional<Boolean> canToggleRendering) {

  public static final SlotTypePredicate ANY = SlotTypePredicate.builder().build();

  public static final Codec<SlotTypePredicate> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
              Codec.withAlternative(Codec.STRING.listOf(), Codec.STRING, List::of)
                  .optionalFieldOf("id", List.of())
                  .forGetter(SlotTypePredicate::id),
              MinMaxBounds.Ints.CODEC
                  .optionalFieldOf("size", MinMaxBounds.Ints.ANY)
                  .forGetter(SlotTypePredicate::size),
              DropRule.CODEC
                  .optionalFieldOf("drop_rule")
                  .forGetter(SlotTypePredicate::dropRule),
              Codec.BOOL
                  .optionalFieldOf("add_cosmetic")
                  .forGetter(SlotTypePredicate::hasCosmetic),
              Codec.BOOL
                  .optionalFieldOf("render_toggle")
                  .forGetter(SlotTypePredicate::canToggleRendering)
          )
          .apply(instance, SlotTypePredicate::new)
  );

  public static final Codec<SlotTypePredicate> FULL_CODEC =
      Codec.withAlternative(
          CODEC,
          Codec.withAlternative(
              Codec.STRING.listOf(),
              Codec.STRING,
              List::of),
          list -> SlotTypePredicate.builder().withId(list.toArray(String[]::new)).build()
      );

  public static final StreamCodec<RegistryFriendlyByteBuf, SlotTypePredicate> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
          SlotTypePredicate::id,
          ByteBufCodecs.fromCodec(MinMaxBounds.Ints.CODEC),
          SlotTypePredicate::size,
          DropRule.STREAM_CODEC.apply(ByteBufCodecs::optional),
          SlotTypePredicate::dropRule,
          ByteBufCodecs.BOOL.apply(ByteBufCodecs::optional),
          SlotTypePredicate::hasCosmetic,
          ByteBufCodecs.BOOL.apply(ByteBufCodecs::optional),
          SlotTypePredicate::canToggleRendering,
          SlotTypePredicate::new
      );

  /**
   * Returns a new builder instance for this slot type predicate.
   *
   * @return A new builder instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Checks for a match on this slot type predicate against a slot identifier.
   *
   * @param slotId The slot identifier to match.
   * @return True if the slot identifier matches this predicate, false otherwise or if the slot type
   *     does not exist for this identifier.
   */
  public boolean matches(String slotId) {
    ISlotType slotType = ISlotType.get(slotId);
    return slotType != null && this.matches(slotType);
  }

  /**
   * Checks for a match on this slot type predicate against a slot type.
   *
   * <p>The only required elements are at least one identifier and a size range. All other elements,
   * if absent as an optional, will be skipped for checking.
   *
   * @param slotType The slot type to match.
   * @return True if the slot type matches this predicate, false otherwise.
   */
  public boolean matches(ISlotType slotType) {

    for (String id : this.id()) {

      if (!slotType.getId().equals(id)) {
        return false;
      }
    }
    return size().matches(slotType.getSize())
        && dropRule().map(dr -> slotType.getDropRule() == dr).orElse(true)
        && hasCosmetic().map(cs -> slotType.hasCosmetic() == cs).orElse(true)
        && canToggleRendering().map(tr -> slotType.canToggleRendering() == tr).orElse(true);
  }

  /**
   * Builder for creating a {@link SlotTypePredicate}.
   */
  public static class Builder {

    private final List<String> slots = new ArrayList<>();
    private MinMaxBounds.Ints size = MinMaxBounds.Ints.ANY;
    private DropRule dropRule = null;
    private Boolean hasCosmetic = null;
    private Boolean canToggleRendering = null;

    private Builder() {
    }

    /**
     * Adds one or more slot identifiers to the list of slots to check.
     *
     * <p>The predicate will match if any of these slot identifiers match.
     *
     * @param slots One or more slot identifiers on the slot type to match.
     * @return This builder instance.
     */
    public SlotTypePredicate.Builder withId(String... slots) {
      this.slots.addAll(Arrays.asList(slots));
      return this;
    }

    /**
     * Sets the size for the slot type.
     *
     * <p>The predicate will match only if the default slot type size is exactly this number.
     *
     * @param size The size for the default slot type set for matching.
     * @return This builder instance.
     */
    public SlotTypePredicate.Builder withSize(int size) {
      this.size = MinMaxBounds.Ints.exactly(size);
      return this;
    }

    /**
     * Sets the size to a range with an optional minimum or maximum as determined by
     * {@link MinMaxBounds.Ints#matches(int)}.
     *
     * <p>The predicate will match if the default slot type size is within the given range.
     *
     * @param size The bounds for the default slot type size to set for matching.
     * @return This builder instance.
     */
    public SlotTypePredicate.Builder withSize(MinMaxBounds.Ints size) {
      this.size = size;
      return this;
    }

    /**
     * Sets the boolean value for whether the slot type has cosmetics.
     *
     * <p>The predicate will match if whether the slot type has cosmetics matches this value.
     *
     * @param flag The boolean value for cosmetics on the slot type.
     * @return This builder instance.
     */
    public SlotTypePredicate.Builder hasCosmetic(boolean flag) {
      this.hasCosmetic = flag;
      return this;
    }

    /**
     * Sets the {@link DropRule} for the slot type.
     *
     * <p>The predicate will match if the slot type is set to the same DropRule.
     *
     * @param dropRule The DropRule for the slot type.
     * @return This builder instance.
     */
    public SlotTypePredicate.Builder withDropRule(DropRule dropRule) {
      this.dropRule = dropRule;
      return this;
    }

    /**
     * Sets the boolean value for whether the slot type can toggle rendering.
     *
     * <p>The predicate will match if whether the slot type can toggle rendering matches this value.
     *
     * @param flag The boolean value for render toggles on the slot type.
     * @return This builder instance.
     */
    public SlotTypePredicate.Builder canToggleRendering(boolean flag) {
      this.canToggleRendering = flag;
      return this;
    }

    /**
     * Creates a new {@link SlotTypePredicate} from this builder.
     *
     * @return A new SlotTypePredicate.
     */
    public SlotTypePredicate build() {
      return new SlotTypePredicate(this.slots,
                                   this.size,
                                   Optional.ofNullable(this.dropRule),
                                   Optional.ofNullable(this.hasCosmetic),
                                   Optional.ofNullable(this.canToggleRendering));
    }
  }
}
