/*
 * Copyright (c) 2018-2024 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package top.theillusivec4.curios.api.common.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.CuriosDataComponents;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.common.slot.SlotTypePredicate;
import top.theillusivec4.curios.api.type.ISlotType;

/**
 * Loot function to create curio attribute modifiers and set them on an ItemStack.
 */
public class SetCurioAttributesFunction extends LootItemConditionalFunction {

  public static final LootItemFunctionType<SetCurioAttributesFunction> TYPE =
      new LootItemFunctionType<>(SetCurioAttributesFunction.CODEC);

  public static final MapCodec<SetCurioAttributesFunction> CODEC = RecordCodecBuilder.mapCodec(
      instance -> commonFields(instance)
          .and(
              instance.group(
                  ExtraCodecs.nonEmptyList(Modifier.CODEC.listOf())
                      .fieldOf("modifiers")
                      .forGetter(function -> function.modifiers),
                  Codec.BOOL.optionalFieldOf("replace", Boolean.TRUE)
                      .forGetter(function -> function.replace)
              )
          )
          .apply(instance, SetCurioAttributesFunction::new)
  );

  final List<Modifier> modifiers;
  final boolean replace;

  SetCurioAttributesFunction(List<LootItemCondition> conditions, List<Modifier> modifiers,
                             boolean replace) {
    super(conditions);
    this.modifiers = ImmutableList.copyOf(modifiers);
    this.replace = replace;
  }

  @Nonnull
  @Override
  public LootItemFunctionType<SetCurioAttributesFunction> getType() {
    return TYPE;
  }

  @Nonnull
  @Override
  public Set<ContextKey<?>> getReferencedContextParams() {
    return this.modifiers.stream()
        .flatMap(modifier -> modifier.amount.getReferencedContextParams().stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  @Nonnull
  @Override
  public ItemStack run(@Nonnull ItemStack stack, @Nonnull LootContext context) {

    if (this.replace) {
      stack.set(CuriosDataComponents.ATTRIBUTE_MODIFIERS,
                this.updateModifiers(context, CurioAttributeModifiers.EMPTY));
    } else {
      CuriosDataComponents
          .updateCurioAttributeModifiers(
              stack,
              curioAttributeModifiers -> this.updateModifiers(context, curioAttributeModifiers));
    }
    return stack;
  }

  private CurioAttributeModifiers updateModifiers(LootContext context,
                                                  CurioAttributeModifiers modifiers) {
    RandomSource randomsource = context.getRandom();

    for (SetCurioAttributesFunction.Modifier modifier : this.modifiers) {
      SlotTypePredicate slotTypePredicate =
          Util.getRandom(modifier.slotTypePredicate(), randomsource);
      modifiers = modifiers.withModifierAdded(
          modifier.attribute(),
          new AttributeModifier(modifier.id(), modifier.amount().getFloat(context),
                                modifier.operation()),
          slotTypePredicate
      );
    }
    return modifiers;
  }

  /**
   * Creates and returns a new {@link ModifierBuilder} for building modifiers on this loot function,
   * using the parameters provided for initial values.
   *
   * <p>The {@link ResourceLocation} used for the location of the attribute modifier will have
   * a suffix appended when applied on an entity during gameplay to avoid conflicts when multiple
   * are present at once.
   *
   * @param id        The default location to use for the attribute modifier.
   * @param attribute The attribute to apply the attribute modifier on.
   * @param operation The operation to use for the attribute modifier.
   * @param amount    The amount to use for the attribute modifier, represented as a
   *                  {@link NumberProvider}.
   * @return A new builder instance.
   */
  public static SetCurioAttributesFunction.ModifierBuilder modifier(
      ResourceLocation id, Holder<Attribute> attribute, AttributeModifier.Operation operation,
      NumberProvider amount) {
    return new SetCurioAttributesFunction.ModifierBuilder(id, attribute, operation, amount);
  }

  /**
   * Creates a returns a new {@link Builder} for building the loot function.
   *
   * @return A new builder instance.
   */
  public static SetCurioAttributesFunction.Builder setAttributes() {
    return new SetCurioAttributesFunction.Builder();
  }

  /**
   * Builder for building a new instance of the {@link SetCurioAttributesFunction} loot function.
   */
  public static class Builder
      extends LootItemConditionalFunction.Builder<SetCurioAttributesFunction.Builder> {

    private final boolean replace;
    private final List<SetCurioAttributesFunction.Modifier> modifiers = Lists.newArrayList();

    /**
     * Creates a new instance of this builder.
     *
     * @param replace True if the modifiers from this builder should replace all existing curio
     *                attribute modifiers on the stack, false otherwise.
     */
    public Builder(boolean replace) {
      this.replace = replace;
    }

    /**
     * Creates a new instance of this builder, with a default value of false for the replace
     * field.
     */
    public Builder() {
      this(false);
    }

    @Nonnull
    protected SetCurioAttributesFunction.Builder getThis() {
      return this;
    }

    /**
     * Adds a curio attribute modifier to this builder, provided as a {@link ModifierBuilder}.
     *
     * <p>The modifier will be built from the provided builder before being added, making the
     * modifier being passed in effectively immutable.
     *
     * @param modifierBuilder The modifier builder to build the modifier from.
     * @return This builder instance.
     */
    public SetCurioAttributesFunction.Builder withModifier(
        SetCurioAttributesFunction.ModifierBuilder modifierBuilder) {
      this.modifiers.add(modifierBuilder.build());
      return this;
    }

    @Nonnull
    @Override
    public LootItemFunction build() {
      return new SetCurioAttributesFunction(this.getConditions(), this.modifiers, this.replace);
    }
  }

  /**
   * Record representing the data to use for the curio attribute modifier.
   *
   * @param id                The default location to be used for the attribute modifier.
   * @param attribute         The attribute to apply the attribute modifier on.
   * @param operation         The operation to use for the attribute modifier.
   * @param amount            The amount to use for the attribute modifier, represented as a
   *                          {@link NumberProvider}.
   * @param slotTypePredicate The {@link SlotTypePredicate} to use for slot matching when applying
   *                          the attribute modifier.
   * @see CurioAttributeModifiers
   */
  public record Modifier(ResourceLocation id, Holder<Attribute> attribute,
                         AttributeModifier.Operation operation, NumberProvider amount,
                         List<SlotTypePredicate> slotTypePredicate) {

    public static final Codec<SetCurioAttributesFunction.Modifier> CODEC =
        RecordCodecBuilder.create(
            modifier -> modifier.group(
                    ResourceLocation.CODEC
                        .fieldOf("id")
                        .forGetter(SetCurioAttributesFunction.Modifier::id),
                    Codec.withAlternative(Attribute.CODEC, SlotAttribute.CODEC)
                        .fieldOf("attribute")
                        .forGetter(SetCurioAttributesFunction.Modifier::attribute),
                    AttributeModifier.Operation.CODEC
                        .fieldOf("operation")
                        .forGetter(SetCurioAttributesFunction.Modifier::operation),
                    NumberProviders.CODEC
                        .fieldOf("amount")
                        .forGetter(SetCurioAttributesFunction.Modifier::amount),
                    SlotTypePredicate.FULL_CODEC
                        .listOf()
                        .optionalFieldOf("slot", List.of())
                        .forGetter(SetCurioAttributesFunction.Modifier::slotTypePredicate)
                )
                .apply(modifier, SetCurioAttributesFunction.Modifier::new)
        );
  }

  /**
   * Builder for building a new instance of a {@link Modifier} entry.
   */
  public static class ModifierBuilder {

    private final ResourceLocation id;
    private final Holder<Attribute> attribute;
    private final AttributeModifier.Operation operation;
    private final NumberProvider amount;
    private final List<SlotTypePredicate> slotTypePredicates = new ArrayList<>();

    /**
     * Constructs a new builder instance with the provided values used for the attribute modifier.
     *
     * <p>The {@link ResourceLocation} used for the location of the attribute modifier will have
     * a suffix appended when applied on an entity during gameplay to avoid conflicts when multiple
     * are present at once.
     *
     * @param id        The default location to use for the attribute modifier.
     * @param attribute The attribute to apply the attribute modifier on.
     * @param operation The operation to use for the attribute modifier.
     * @param amount    The amount to use for the attribute modifier, represented as a
     *                  {@link NumberProvider}.
     */
    public ModifierBuilder(ResourceLocation id, Holder<Attribute> attribute,
                           AttributeModifier.Operation operation, NumberProvider amount) {
      this.id = id;
      this.attribute = attribute;
      this.operation = operation;
      this.amount = amount;
    }

    /**
     * Adds one or more slot identifiers to use for slot type matching when deciding when to apply
     * attribute modifiers.
     *
     * <p>Each call to this method will generate a new list of slot identifiers, which are each
     * independent of each other. Only one entry is chosen to be applied on each run of the loot
     * function, with each entry given equal chance.
     *
     * <p>This method should only be called once for each builder instance if the desired behavior
     * requires a single set of slot identifiers to match against. Conversely, it should be called
     * for each identifier if the desired behavior requires a random selection amongst all of them.
     *
     * @param id An array of one or more slot identifiers for slot types.
     * @return This builder instance.
     * @see SlotTypePredicate
     */
    public SetCurioAttributesFunction.ModifierBuilder forSlotId(String... id) {
      this.slotTypePredicates.add(SlotTypePredicate.builder().withId(id).build());
      return this;
    }

    /**
     * Adds one or more {@link ISlotType} to use for matching when deciding when to apply attribute
     * modifiers.
     *
     * <p>Each call to this method will generate a new list of slot types, which are each
     * independent of each other. Only one entry is chosen to be applied on each run of the loot
     * function, with each entry given equal chance.
     *
     * <p>This method should only be called once for each builder instance if the desired behavior
     * requires a single set of slot types to match against. Conversely, it should be called for
     * each slot type if the desired behavior requires a random selection amongst all of them.
     *
     * @param slotType An array of one or more slot types.
     * @return This builder instance.
     * @see SlotTypePredicate
     */
    public SetCurioAttributesFunction.ModifierBuilder forSlot(ISlotType... slotType) {
      this.slotTypePredicates.add(SlotTypePredicate.builder()
                                      .withId(Arrays.stream(slotType).map(ISlotType::getId)
                                                  .toArray(String[]::new))
                                      .build());
      return this;
    }

    /**
     * Adds one or more {@link SlotTypePredicate} to use for matching when deciding when to apply
     * attribute modifiers.
     *
     * <p>Each call to this method will generate a new list of predicates, which are each
     * independent of each other. Only one entry is chosen to be applied on each run of the loot
     * function, with each entry given equal chance.
     *
     * <p>This method should only be called once for each builder instance if the desired behavior
     * requires a single set of predicates to match against. Conversely, it should be called for
     * each predicate if the desired behavior requires a random selection amongst all of them.
     *
     * @param slotTypePredicate An array of one or more slot type predicates.
     * @return This builder instance.
     */
    public SetCurioAttributesFunction.ModifierBuilder forSlot(
        SlotTypePredicate... slotTypePredicate) {
      this.slotTypePredicates.addAll(Arrays.asList(slotTypePredicate));
      return this;
    }

    /**
     * Creates a returns a new {@link Modifier} with this builder's values.
     *
     * @return A new attribute modifier entry.
     */
    public SetCurioAttributesFunction.Modifier build() {
      return new SetCurioAttributesFunction.Modifier(this.id, this.attribute, this.operation,
                                                     this.amount, this.slotTypePredicates);
    }
  }
}
