/*
 * Copyright (c) 2018-2020 C4
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
 */

package top.theillusivec4.curios.common;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.api.type.util.ICuriosHelper;

public class CuriosHelper implements ICuriosHelper {

  private static final Map<String, SlotAttributeWrapper> SLOT_ATTRIBUTES = new HashMap<>();

  private static TriConsumer<String, Integer, LivingEntity> brokenCurioConsumerLegacy;
  private static Consumer<SlotContext> brokenCurioConsumer;

  @Override
  public LazyOptional<ICurio> getCurio(ItemStack stack) {
    return stack.getCapability(CuriosCapability.ITEM);
  }

  @Override
  public LazyOptional<ICuriosItemHandler> getCuriosHandler(
      @Nonnull final LivingEntity livingEntity) {
    return livingEntity.getCapability(CuriosCapability.INVENTORY);
  }

  @SuppressWarnings("deprecation")
  @Override
  public Set<String> getCurioTags(Item item) {
    return item.builtInRegistryHolder().tags().map(TagKey::location)
        .filter(resourceLocation -> resourceLocation.getNamespace().equals(CuriosApi.MODID))
        .map(ResourceLocation::getPath).collect(Collectors.toSet());
  }

  @Override
  public LazyOptional<IItemHandlerModifiable> getEquippedCurios(LivingEntity livingEntity) {
    return CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).lazyMap(handler -> {
      Map<String, ICurioStacksHandler> curios = handler.getCurios();
      IItemHandlerModifiable[] itemHandlers = new IItemHandlerModifiable[curios.size()];
      int index = 0;

      for (ICurioStacksHandler stacksHandler : curios.values()) {

        if (index < itemHandlers.length) {
          itemHandlers[index] = stacksHandler.getStacks();
          index++;
        }
      }
      return new CombinedInvWrapper(itemHandlers);
    });
  }

  @Override
  public void setEquippedCurio(@NotNull LivingEntity livingEntity, String identifier, int index,
                               ItemStack stack) {
    getCuriosHandler(livingEntity).ifPresent(handler -> {
      Map<String, ICurioStacksHandler> curios = handler.getCurios();
      ICurioStacksHandler stacksHandler = curios.get(identifier);

      if (stacksHandler != null) {
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        if (index < stackHandler.getSlots()) {
          stackHandler.setStackInSlot(index, stack);
        }
      }
    });
  }

  @Override
  public Optional<SlotResult> findFirstCurio(@Nonnull LivingEntity livingEntity, Item item) {
    return findFirstCurio(livingEntity, (stack) -> stack.getItem() == item);
  }

  @Override
  public Optional<SlotResult> findFirstCurio(@Nonnull LivingEntity livingEntity,
                                             Predicate<ItemStack> filter) {
    SlotResult result = getCuriosHandler(livingEntity).map(handler -> {
      Map<String, ICurioStacksHandler> curios = handler.getCurios();

      for (String id : curios.keySet()) {
        ICurioStacksHandler stacksHandler = curios.get(id);
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        for (int i = 0; i < stackHandler.getSlots(); i++) {
          ItemStack stack = stackHandler.getStackInSlot(i);

          if (!stack.isEmpty() && filter.test(stack)) {
            NonNullList<Boolean> renderStates = stacksHandler.getRenders();
            return new SlotResult(new SlotContext(id, livingEntity, i, false,
                renderStates.size() > i && renderStates.get(i)), stack);
          }
        }
      }
      return new SlotResult(null, ItemStack.EMPTY);
    }).orElse(new SlotResult(null, ItemStack.EMPTY));
    return (result.stack().isEmpty() || result.slotContext() == null) ? Optional.empty() :
        Optional.of(result);
  }

  @Override
  public List<SlotResult> findCurios(@Nonnull LivingEntity livingEntity, Item item) {
    return findCurios(livingEntity, (stack) -> stack.getItem() == item);
  }

  @Override
  public List<SlotResult> findCurios(@Nonnull LivingEntity livingEntity,
                                     Predicate<ItemStack> filter) {
    List<SlotResult> result = new ArrayList<>();
    getCuriosHandler(livingEntity).ifPresent(handler -> {
      Map<String, ICurioStacksHandler> curios = handler.getCurios();

      for (String id : curios.keySet()) {
        ICurioStacksHandler stacksHandler = curios.get(id);
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        for (int i = 0; i < stackHandler.getSlots(); i++) {
          ItemStack stack = stackHandler.getStackInSlot(i);

          if (!stack.isEmpty() && filter.test(stack)) {
            NonNullList<Boolean> renderStates = stacksHandler.getRenders();
            result.add(new SlotResult(new SlotContext(id, livingEntity, i, false,
                renderStates.size() > i && renderStates.get(i)), stack));
          }
        }
      }
    });
    return result;
  }

  @Override
  public List<SlotResult> findCurios(@NotNull LivingEntity livingEntity, String... identifiers) {
    List<SlotResult> result = new ArrayList<>();
    Set<String> ids = new HashSet<>(List.of(identifiers));
    getCuriosHandler(livingEntity).ifPresent(handler -> {
      Map<String, ICurioStacksHandler> curios = handler.getCurios();

      for (String id : curios.keySet()) {

        if (ids.contains(id)) {
          ICurioStacksHandler stacksHandler = curios.get(id);
          IDynamicStackHandler stackHandler = stacksHandler.getStacks();

          for (int i = 0; i < stackHandler.getSlots(); i++) {
            ItemStack stack = stackHandler.getStackInSlot(i);

            if (!stack.isEmpty()) {
              NonNullList<Boolean> renderStates = stacksHandler.getRenders();
              result.add(new SlotResult(new SlotContext(id, livingEntity, i, false,
                  renderStates.size() > i && renderStates.get(i)), stack));
            }
          }
        }
      }
    });
    return result;
  }

  @Override
  public Optional<SlotResult> findCurio(@Nonnull LivingEntity livingEntity,
                                        String identifier, int index) {
    SlotResult result = getCuriosHandler(livingEntity).map(handler -> {
      Map<String, ICurioStacksHandler> curios = handler.getCurios();
      ICurioStacksHandler stacksHandler = curios.get(identifier);

      if (stacksHandler != null) {
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        if (index < stackHandler.getSlots()) {
          ItemStack stack = stackHandler.getStackInSlot(index);

          if (!stack.isEmpty()) {
            NonNullList<Boolean> renderStates = stacksHandler.getRenders();
            return new SlotResult(new SlotContext(identifier, livingEntity, index, false,
                renderStates.size() > index && renderStates.get(index)), stack);
          }
        }
      }
      return new SlotResult(null, ItemStack.EMPTY);
    }).orElse(new SlotResult(null, ItemStack.EMPTY));
    return (result.stack().isEmpty() || result.slotContext() == null) ? Optional.empty() :
        Optional.of(result);
  }

  @Nonnull
  @Override
  public Optional<ImmutableTriple<String, Integer, ItemStack>> findEquippedCurio(Item item,
                                                                                 @Nonnull
                                                                                 final LivingEntity livingEntity) {
    return findEquippedCurio((stack) -> stack.getItem() == item, livingEntity);
  }

  @Nonnull
  @Override
  public Optional<ImmutableTriple<String, Integer, ItemStack>> findEquippedCurio(
      Predicate<ItemStack> filter, @Nonnull final LivingEntity livingEntity) {

    ImmutableTriple<String, Integer, ItemStack> result = getCuriosHandler(livingEntity)
        .map(handler -> {
          Map<String, ICurioStacksHandler> curios = handler.getCurios();

          for (String id : curios.keySet()) {
            ICurioStacksHandler stacksHandler = curios.get(id);
            IDynamicStackHandler stackHandler = stacksHandler.getStacks();

            for (int i = 0; i < stackHandler.getSlots(); i++) {
              ItemStack stack = stackHandler.getStackInSlot(i);

              if (!stack.isEmpty() && filter.test(stack)) {
                return new ImmutableTriple<>(id, i, stack);
              }
            }
          }
          return new ImmutableTriple<>("", 0, ItemStack.EMPTY);
        }).orElse(new ImmutableTriple<>("", 0, ItemStack.EMPTY));

    return result.getLeft().isEmpty() ? Optional.empty() : Optional.of(result);
  }

  @Override
  public Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier,
                                                                      ItemStack stack) {
    return getAttributeModifiers(new SlotContext(identifier, null, 0, false, true),
        UUID.randomUUID(), stack);
  }

  @Override
  public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext,
                                                                      UUID uuid, ItemStack stack) {
    Multimap<Attribute, AttributeModifier> multimap;

    if (stack.getTag() != null && stack.getTag().contains("CurioAttributeModifiers", 9)) {
      multimap = HashMultimap.create();
      ListTag listnbt = stack.getTag().getList("CurioAttributeModifiers", 10);
      String identifier = slotContext.identifier();

      for (int i = 0; i < listnbt.size(); ++i) {
        CompoundTag compoundnbt = listnbt.getCompound(i);

        if (compoundnbt.getString("Slot").equals(identifier)) {
          ResourceLocation rl = ResourceLocation.tryParse(compoundnbt.getString("AttributeName"));
          UUID id = uuid;

          if (rl != null) {

            if (compoundnbt.contains("UUID")) {
              id = compoundnbt.getUUID("UUID");
            }

            if (id.getLeastSignificantBits() != 0L && id.getMostSignificantBits() != 0L) {
              AttributeModifier.Operation operation =
                  AttributeModifier.Operation.fromValue(compoundnbt.getInt("Operation"));
              double amount = compoundnbt.getDouble("Amount");
              String name = compoundnbt.getString("Name");

              if (rl.getNamespace().equals("curios")) {
                String identifier1 = rl.getPath();

                if (CuriosApi.getSlotHelper().getSlotType(identifier1).isPresent()) {
                  CuriosApi.getCuriosHelper()
                      .addSlotModifier(multimap, identifier1, id, amount, operation);
                }
              } else {
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(rl);

                if (attribute != null) {
                  multimap.put(attribute, new AttributeModifier(id, name, amount, operation));
                }
              }
            }
          }
        }
      }
      return multimap;
    }
    return getCurio(stack).map(curio -> curio.getAttributeModifiers(slotContext, uuid))
        .orElse(HashMultimap.create());
  }

  @Override
  public void addSlotModifier(Multimap<Attribute, AttributeModifier> map, String identifier,
                              UUID uuid, double amount, AttributeModifier.Operation operation) {
    map.put(getOrCreateSlotAttribute(identifier),
        new AttributeModifier(uuid, identifier, amount, operation));
  }

  @Override
  public void addSlotModifier(ItemStack stack, String identifier, String name, UUID uuid,
                              double amount, AttributeModifier.Operation operation, String slot) {
    addModifier(stack, getOrCreateSlotAttribute(identifier), name, uuid, amount, operation, slot);
  }

  @Override
  public void addModifier(ItemStack stack, Attribute attribute, String name, UUID uuid,
                          double amount, AttributeModifier.Operation operation, String slot) {
    CompoundTag tag = stack.getOrCreateTag();

    if (!tag.contains("CurioAttributeModifiers", 9)) {
      tag.put("CurioAttributeModifiers", new ListTag());
    }
    ListTag listtag = tag.getList("CurioAttributeModifiers", 10);
    CompoundTag compoundtag = new CompoundTag();
    compoundtag.putString("Name", name);
    compoundtag.putDouble("Amount", amount);
    compoundtag.putInt("Operation", operation.toValue());

    if (uuid != null) {
      compoundtag.putUUID("UUID", uuid);
    }
    String id = "";

    if (attribute instanceof SlotAttributeWrapper wrapper) {
      id = "curios:" + wrapper.identifier;
    } else {
      ResourceLocation rl = ForgeRegistries.ATTRIBUTES.getKey(attribute);

      if (rl != null) {
        id = rl.toString();
      }
    }

    if (!id.isEmpty()) {
      compoundtag.putString("AttributeName", id);
    }
    compoundtag.putString("Slot", slot);
    listtag.add(compoundtag);
  }

  @Override
  public boolean isStackValid(SlotContext slotContext, ItemStack stack) {
    String id = slotContext.identifier();
    Set<String> tags = getCurioTags(stack.getItem());
    return (!tags.isEmpty() && id.equals(SlotTypePreset.CURIO.getIdentifier())) ||
        tags.contains(id) || tags.contains(SlotTypePreset.CURIO.getIdentifier());
  }

  @Override
  public void onBrokenCurio(SlotContext slotContext) {
    brokenCurioConsumer.accept(slotContext);
  }

  @Override
  public void setBrokenCurioConsumer(Consumer<SlotContext> consumer) {

    if (brokenCurioConsumer == null) {
      brokenCurioConsumer = consumer;
    }
  }

  @Override
  public void onBrokenCurio(String id, int index, LivingEntity damager) {
    brokenCurioConsumerLegacy.accept(id, index, damager);
  }

  @Override
  public void setBrokenCurioConsumer(TriConsumer<String, Integer, LivingEntity> consumer) {

    if (brokenCurioConsumerLegacy == null) {
      brokenCurioConsumerLegacy = consumer;
    }
  }

  public static SlotAttributeWrapper getOrCreateSlotAttribute(String identifier) {
    return SLOT_ATTRIBUTES.computeIfAbsent(identifier, SlotAttributeWrapper::new);
  }

  public static class SlotAttributeWrapper extends Attribute {

    public final String identifier;

    private SlotAttributeWrapper(String identifier) {
      super("curios.slot." + identifier, 0);
      this.identifier = identifier;
    }
  }
}
