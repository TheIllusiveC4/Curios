package top.theillusivec4.curios.common.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.CuriosHelper;

public class SetCurioAttributesFunction extends LootItemConditionalFunction {

  public static LootItemFunctionType TYPE = null;

  final List<Modifier> modifiers;

  SetCurioAttributesFunction(LootItemCondition[] conditions, List<Modifier> modifiers) {
    super(conditions);
    this.modifiers = ImmutableList.copyOf(modifiers);
  }

  public static void register() {
    TYPE = Registry.register(Registry.LOOT_FUNCTION_TYPE,
        new ResourceLocation(CuriosApi.MODID, "set_curio_attributes"),
        new LootItemFunctionType(new SetCurioAttributesFunction.Serializer()));
  }

  @Nonnull
  public LootItemFunctionType getType() {
    return LootItemFunctions.SET_ATTRIBUTES;
  }

  @Nonnull
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return this.modifiers.stream()
        .flatMap((mod) -> mod.amount.getReferencedContextParams().stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  @Nonnull
  public ItemStack run(@Nonnull ItemStack stack, LootContext context) {
    Random random = context.getRandom();

    for (Modifier modifier : this.modifiers) {
      UUID uuid = modifier.id;
      String slot = Util.getRandom(modifier.slots, random);

      if (modifier.attribute instanceof CuriosHelper.SlotAttributeWrapper wrapper) {
        CuriosApi.getCuriosHelper().addSlotModifier(stack, wrapper.identifier, modifier.name, uuid,
            modifier.amount.getFloat(context), modifier.operation, slot);
      } else {
        CuriosApi.getCuriosHelper().addModifier(stack, modifier.attribute, modifier.name, uuid,
            modifier.amount.getFloat(context), modifier.operation, slot);
      }
    }
    return stack;
  }

  static class Modifier {
    final String name;
    final Attribute attribute;
    final AttributeModifier.Operation operation;
    final NumberProvider amount;
    @Nullable
    final UUID id;
    final String[] slots;

    Modifier(String name, Attribute attribute, AttributeModifier.Operation operation,
             NumberProvider amount, String[] slots, @Nullable UUID uuid) {
      this.name = name;
      this.attribute = attribute;
      this.operation = operation;
      this.amount = amount;
      this.id = uuid;
      this.slots = slots;
    }

    public JsonObject serialize(JsonSerializationContext context) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("name", this.name);
      ResourceLocation rl;

      if (this.attribute instanceof CuriosHelper.SlotAttributeWrapper wrapper) {
        rl = new ResourceLocation(CuriosApi.MODID, wrapper.identifier);
      } else {
        rl = ForgeRegistries.ATTRIBUTES.getKey(this.attribute);
      }

      if (rl != null) {
        jsonobject.addProperty("attribute", rl.toString());
      }
      jsonobject.addProperty("operation", operationToString(this.operation));
      jsonobject.add("amount", context.serialize(this.amount));

      if (this.id != null) {
        jsonobject.addProperty("id", this.id.toString());
      }

      if (this.slots.length == 1) {
        jsonobject.addProperty("slot", this.slots[0]);
      } else {
        JsonArray jsonarray = new JsonArray();

        for (String slot : this.slots) {
          jsonarray.add(new JsonPrimitive(slot));
        }
        jsonobject.add("slot", jsonarray);
      }
      return jsonobject;
    }

    public static Modifier deserialize(JsonObject object, JsonDeserializationContext context) {
      String s = GsonHelper.getAsString(object, "name");
      ResourceLocation resourcelocation =
          new ResourceLocation(GsonHelper.getAsString(object, "attribute"));
      Attribute attribute;

      if (resourcelocation.getNamespace().equals("curios")) {
        String identifier = resourcelocation.getPath();

        if (CuriosApi.getSlotHelper().getSlotType(identifier).isEmpty()) {
          throw new JsonSyntaxException("Unknown curios slot type: " + identifier);
        }
        attribute = CuriosHelper.getOrCreateSlotAttribute(identifier);
      } else {
        attribute = ForgeRegistries.ATTRIBUTES.getValue(resourcelocation);
      }

      if (attribute == null) {
        throw new JsonSyntaxException("Unknown attribute: " + resourcelocation);
      } else {
        AttributeModifier.Operation operation =
            operationFromString(GsonHelper.getAsString(object, "operation"));
        NumberProvider numberprovider =
            GsonHelper.getAsObject(object, "amount", context, NumberProvider.class);
        UUID uuid = null;
        String[] slots;

        if (GsonHelper.isStringValue(object, "slot")) {
          slots = new String[] {GsonHelper.getAsString(object, "slot")};
        } else {

          if (!GsonHelper.isArrayNode(object, "slot")) {
            throw new JsonSyntaxException(
                "Invalid or missing attribute modifier slot; must be either string or array of strings.");
          }
          JsonArray jsonarray = GsonHelper.getAsJsonArray(object, "slot");
          slots = new String[jsonarray.size()];
          int i = 0;

          for (JsonElement jsonelement : jsonarray) {
            slots[i++] = GsonHelper.convertToString(jsonelement, "slot");
          }

          if (slots.length == 0) {
            throw new JsonSyntaxException(
                "Invalid attribute modifier slot; must contain at least one entry.");
          }
        }

        if (object.has("id")) {
          String s1 = GsonHelper.getAsString(object, "id");

          try {
            uuid = UUID.fromString(s1);
          } catch (IllegalArgumentException illegalargumentexception) {
            throw new JsonSyntaxException(
                "Invalid attribute modifier id '" + s1 + "' (must be UUID format, with dashes)");
          }
        }
        return new Modifier(s, attribute, operation, numberprovider, slots, uuid);
      }
    }

    private static String operationToString(AttributeModifier.Operation operation) {
      return switch (operation) {
        case ADDITION -> "addition";
        case MULTIPLY_BASE -> "multiply_base";
        case MULTIPLY_TOTAL -> "multiply_total";
      };
    }

    private static AttributeModifier.Operation operationFromString(String operation) {
      return switch (operation) {
        case "addition" -> AttributeModifier.Operation.ADDITION;
        case "multiply_base" -> AttributeModifier.Operation.MULTIPLY_BASE;
        case "multiply_total" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
        default -> throw new JsonSyntaxException(
            "Unknown attribute modifier operation " + operation);
      };
    }
  }

  public static class Serializer
      extends LootItemConditionalFunction.Serializer<SetCurioAttributesFunction> {

    public void serialize(@Nonnull JsonObject object,
                          @Nonnull SetCurioAttributesFunction function,
                          @Nonnull JsonSerializationContext context) {
      super.serialize(object, function, context);
      JsonArray jsonarray = new JsonArray();

      for (Modifier modifier : function.modifiers) {
        jsonarray.add(modifier.serialize(context));
      }
      object.add("modifiers", jsonarray);
    }

    @Nonnull
    public SetCurioAttributesFunction deserialize(@Nonnull JsonObject object,
                                                  @Nonnull JsonDeserializationContext context,
                                                  @Nonnull LootItemCondition[] conditions) {
      JsonArray jsonarray = GsonHelper.getAsJsonArray(object, "modifiers");
      List<Modifier> list = Lists.newArrayListWithExpectedSize(jsonarray.size());

      for (JsonElement jsonelement : jsonarray) {
        list.add(
            Modifier.deserialize(GsonHelper.convertToJsonObject(jsonelement, "modifier"), context));
      }

      if (list.isEmpty()) {
        throw new JsonSyntaxException("Invalid attribute modifiers array; cannot be empty");
      } else {
        return new SetCurioAttributesFunction(conditions, list);
      }
    }
  }
}
