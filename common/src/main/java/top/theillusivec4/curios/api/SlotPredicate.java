package top.theillusivec4.curios.api;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.ExtraCodecs;

public record SlotPredicate(Optional<List<String>> slots, MinMaxBounds.Ints index) {

  public static final Codec<SlotPredicate> CODEC = RecordCodecBuilder.create(
      slotPredicateInstance -> slotPredicateInstance.group(
              ExtraCodecs.strictOptionalField(Codec.STRING.listOf(), "slots")
                  .forGetter(SlotPredicate::slots),
              ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "index", MinMaxBounds.Ints.ANY)
                  .forGetter(SlotPredicate::index)
          )
          .apply(slotPredicateInstance, SlotPredicate::new)
  );

  public boolean matches(SlotContext slotContext) {

    if (this.slots.map(sl -> !sl.contains(slotContext.identifier())).orElse(false)) {
      return false;
    } else {
      return this.index.matches(slotContext.index());
    }
  }

  public static class Builder {

    private Set<String> identifiers = new HashSet<>();
    private MinMaxBounds.Ints indices = MinMaxBounds.Ints.ANY;

    private Builder() {
    }

    public static SlotPredicate.Builder slot() {
      return new SlotPredicate.Builder();
    }

    public SlotPredicate.Builder of(String... identifiers) {
      this.identifiers = Stream.of(identifiers).collect(ImmutableSet.toImmutableSet());
      return this;
    }

    public SlotPredicate.Builder withIndex(MinMaxBounds.Ints index) {
      this.indices = index;
      return this;
    }

    public SlotPredicate build() {
      return new SlotPredicate(Optional.of(this.identifiers.stream().toList()), this.indices);
    }
  }
}
