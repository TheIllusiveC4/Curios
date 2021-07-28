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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
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
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.api.type.util.ICuriosHelper;

public class CuriosHelper implements ICuriosHelper {

  private static TriConsumer<String, Integer, LivingEntity> brokenCurioConsumer;

  @Override
  public LazyOptional<ICurio> getCurio(ItemStack stack) {
    return stack.getCapability(CuriosCapability.ITEM);
  }

  @Override
  public LazyOptional<ICuriosItemHandler> getCuriosHandler(
      @Nonnull final LivingEntity livingEntity) {
    return livingEntity.getCapability(CuriosCapability.INVENTORY);
  }

  @Override
  public Set<String> getCurioTags(Item item) {
    return item.getTags().stream().filter(tag -> tag.getNamespace().equals(CuriosApi.MODID))
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

        if (!compoundnbt.contains("Slot", 8) || compoundnbt.getString("Slot").equals(identifier)) {
          Attribute attribute = ForgeRegistries.ATTRIBUTES
              .getValue(ResourceLocation.tryParse(compoundnbt.getString("AttributeName")));

          if (attribute != null) {
            AttributeModifier attributemodifier = AttributeModifier.load(compoundnbt);

            if (attributemodifier != null
                && attributemodifier.getId().getLeastSignificantBits() != 0L
                && attributemodifier.getId().getMostSignificantBits() != 0L) {
              multimap.put(attribute, attributemodifier);
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
  public boolean isStackValid(SlotContext slotContext, ItemStack stack) {
    String id = slotContext.identifier();
    Set<String> tags = getCurioTags(stack.getItem());
    return (!tags.isEmpty() && id.equals(SlotTypePreset.CURIO.getIdentifier())) ||
        tags.contains(id) || tags.contains(SlotTypePreset.CURIO.getIdentifier());
  }

  @Override
  public void onBrokenCurio(String id, int index, LivingEntity damager) {
    brokenCurioConsumer.accept(id, index, damager);
  }

  @Override
  public void setBrokenCurioConsumer(TriConsumer<String, Integer, LivingEntity> consumer) {

    if (brokenCurioConsumer == null) {
      brokenCurioConsumer = consumer;
    }
  }
}
