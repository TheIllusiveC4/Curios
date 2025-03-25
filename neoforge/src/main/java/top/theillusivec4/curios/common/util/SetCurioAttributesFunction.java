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

package top.theillusivec4.curios.common.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.common.CuriosRegistry;

public class SetCurioAttributesFunction extends LootItemConditionalFunction {

  public static final MapCodec<SetCurioAttributesFunction> CODEC = RecordCodecBuilder.mapCodec(
      instance -> commonFields(instance)
          .and(
              instance.group(
                  ExtraCodecs.nonEmptyList(Modifier.MODIFIER_CODEC.listOf())
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
  public LootItemFunctionType<SetCurioAttributesFunction> getType() {
    return CuriosRegistry.CURIO_ATTRIBUTES.get();
  }

  @Nonnull
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return this.modifiers.stream()
        .flatMap((mod) -> mod.amount.getReferencedContextParams().stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  @Nonnull
  public ItemStack run(@Nonnull ItemStack stack, LootContext context) {
    RandomSource random = context.getRandom();

    for (Modifier modifier : this.modifiers) {
      String slot = Util.getRandom(modifier.slots, random);

      if (modifier.attribute.value() instanceof SlotAttribute wrapper) {
        CuriosApi.addSlotModifier(stack, wrapper.getIdentifier(), modifier.id,
            modifier.amount.getFloat(context), modifier.operation, slot);
      } else {
        CuriosApi.addModifier(stack, modifier.attribute, modifier.id,
            modifier.amount.getFloat(context), modifier.operation, slot);
      }
    }
    return stack;
  }

  record Modifier(Holder<Attribute> attribute, AttributeModifier.Operation operation,
                  NumberProvider amount, ResourceLocation id, List<String> slots) {

    private static final Codec<List<String>> SLOTS_CODEC = ExtraCodecs.nonEmptyList(
        Codec.either(Codec.STRING, Codec.list(Codec.STRING))
            .xmap((either) -> either.map(List::of, Function.identity()),
                (list) -> list.size() == 1 ? Either.left(list.getFirst()) : Either.right(list)));

    private static final Codec<Holder<Attribute>> ATTRIBUTE_CODEC = new PrimitiveCodec<>() {
      @Override
      public <T> DataResult<Holder<Attribute>> read(DynamicOps<T> ops, T input) {
        return ops.getStringValue(input).map(name -> {
          ResourceLocation rl = ResourceLocation.tryParse(name);

          if (rl == null) {
            return null;
          }
          Holder<Attribute> attribute;

          if (rl.getNamespace().equals("curios")) {
            String identifier = rl.getPath();

            if (CuriosApi.getSlot(identifier, false).isEmpty()) {
              throw new JsonSyntaxException("Unknown curios slot type: " + identifier);
            }
            attribute = SlotAttribute.getOrCreate(identifier);
          } else {
            attribute = BuiltInRegistries.ATTRIBUTE.getHolder(rl).orElse(null);
          }
          return attribute;
        });
      }

      @Override
      public <T> T write(DynamicOps<T> ops, Holder<Attribute> value) {
        ResourceLocation rl;

        if (value.value() instanceof SlotAttribute wrapper) {
          rl = ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, wrapper.getIdentifier());
        } else {
          rl = BuiltInRegistries.ATTRIBUTE.getKey(value.value());
        }
        return rl != null ? ops.createString(rl.toString()) : ops.empty();
      }
    };
    public static final Codec<Modifier> MODIFIER_CODEC =
        RecordCodecBuilder.create((instance) -> instance.group(
                ATTRIBUTE_CODEC.fieldOf("attribute")
                    .forGetter(Modifier::attribute),
                AttributeModifier.Operation.CODEC.fieldOf("operation")
                    .forGetter(Modifier::operation),
                NumberProviders.CODEC.fieldOf("amount")
                    .forGetter(Modifier::amount),
                ResourceLocation.CODEC.fieldOf("id")
                    .forGetter(Modifier::id),
                SLOTS_CODEC.fieldOf("slot")
                    .forGetter(Modifier::slots))
            .apply(instance, Modifier::new));
  }
}
