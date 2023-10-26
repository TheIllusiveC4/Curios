package top.theillusivec4.curios.common.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nonnull;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
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
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotAttribute;

public class SetCurioAttributesFunction extends LootItemConditionalFunction {

  public static final Codec<SetCurioAttributesFunction> CODEC = RecordCodecBuilder.create(
      (instance) -> commonFields(instance).and(
              ExtraCodecs.nonEmptyList(Modifier.MODIFIER_CODEC.listOf()).fieldOf("modifiers")
                  .forGetter((function) -> function.modifiers))
          .apply(instance, SetCurioAttributesFunction::new));
  public static LootItemFunctionType TYPE = null;

  final List<Modifier> modifiers;

  SetCurioAttributesFunction(List<LootItemCondition> conditions, List<Modifier> modifiers) {
    super(conditions);
    this.modifiers = ImmutableList.copyOf(modifiers);
  }

  public static void register() {
    TYPE = Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE,
        new ResourceLocation(CuriosApi.MODID, "set_curio_attributes"),
        new LootItemFunctionType(CODEC));
  }

  @Nonnull
  public LootItemFunctionType getType() {
    return TYPE;
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
      UUID uuid = modifier.id.orElse(null);
      String slot = Util.getRandom(modifier.slots, random);

      if (modifier.attribute instanceof SlotAttribute wrapper) {
        CuriosApi.addSlotModifier(stack, wrapper.getIdentifier(), modifier.name, uuid,
            modifier.amount.getFloat(context), modifier.operation, slot);
      } else {
        CuriosApi.addModifier(stack, modifier.attribute, modifier.name, uuid,
            modifier.amount.getFloat(context), modifier.operation, slot);
      }
    }
    return stack;
  }

  record Modifier(String name, Attribute attribute, AttributeModifier.Operation operation,
                  NumberProvider amount, Optional<UUID> id, List<String> slots) {

    private static final Codec<List<String>> SLOTS_CODEC = ExtraCodecs.nonEmptyList(
        Codec.either(Codec.STRING, Codec.list(Codec.STRING))
            .xmap((either) -> either.map(List::of, Function.identity()),
                (list) -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list)));

    private static final Codec<Attribute> ATTRIBUTE_CODEC = new PrimitiveCodec<>() {
      @Override
      public <T> DataResult<Attribute> read(DynamicOps<T> ops, T input) {
        return ops.getStringValue(input).map(name -> {
          ResourceLocation rl = ResourceLocation.tryParse(name);

          if (rl == null) {
            return null;
          }
          Attribute attribute;

          if (rl.getNamespace().equals("curios")) {
            String identifier = rl.getPath();

            if (CuriosApi.getSlot(identifier).isEmpty()) {
              throw new JsonSyntaxException("Unknown curios slot type: " + identifier);
            }
            attribute = SlotAttribute.getOrCreate(identifier);
          } else {
            attribute = ForgeRegistries.ATTRIBUTES.getValue(rl);
          }
          return attribute;
        });
      }

      @Override
      public <T> T write(DynamicOps<T> ops, Attribute value) {
        ResourceLocation rl;

        if (value instanceof SlotAttribute wrapper) {
          rl = new ResourceLocation(CuriosApi.MODID, wrapper.getIdentifier());
        } else {
          rl = ForgeRegistries.ATTRIBUTES.getKey(value);
        }
        return rl != null ? ops.createString(rl.toString()) : ops.empty();
      }
    };
    public static final Codec<SetCurioAttributesFunction.Modifier> MODIFIER_CODEC =
        RecordCodecBuilder.create((instance) -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Modifier::name),
                ATTRIBUTE_CODEC.fieldOf("attribute")
                    .forGetter(Modifier::attribute),
                AttributeModifier.Operation.CODEC.fieldOf("operation")
                    .forGetter(Modifier::operation),
                NumberProviders.CODEC.fieldOf("amount")
                    .forGetter(Modifier::amount),
                ExtraCodecs.strictOptionalField(
                    UUIDUtil.STRING_CODEC, "id").forGetter(Modifier::id),
                SLOTS_CODEC.fieldOf("slot").forGetter(Modifier::slots))
            .apply(instance, Modifier::new));
  }
}
