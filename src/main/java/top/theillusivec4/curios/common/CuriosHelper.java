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
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.util.TriConsumer;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosComponent;
import top.theillusivec4.curios.api.type.component.ICurio;
import top.theillusivec4.curios.api.type.component.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.component.IRenderableCurio;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.api.type.util.ICuriosHelper;

public class CuriosHelper implements ICuriosHelper {

  private static TriConsumer<String, Integer, LivingEntity> brokenCurioConsumer;

  @Override
  public Optional<ICurio> getCurio(ItemStack stack) {
    return CuriosComponent.ITEM.maybeGet(stack);
  }

  @Override
  public Optional<IRenderableCurio> getRenderableCurio(ItemStack stack) {
    return CuriosComponent.ITEM_RENDER.maybeGet(stack);
  }

  @Override
  public Optional<ICuriosItemHandler> getCuriosHandler(final LivingEntity livingEntity) {
    return CuriosComponent.INVENTORY.maybeGet(livingEntity);
  }

  @Override
  public Set<String> getCurioTags(Item item) {
    List<Identifier> list = Lists.newArrayList();

    for (Entry<Identifier, Tag<Item>> identifierTagEntry : ItemTags.getTagGroup().getTags()
        .entrySet()) {
      if ((identifierTagEntry.getValue()).contains(item)) {
        list.add(identifierTagEntry.getKey());
      }
    }
    return list.stream().filter(tag -> tag.getNamespace().equals(CuriosApi.MODID))
        .map(Identifier::getPath).collect(Collectors.toSet());
  }

  @Override
  public Optional<ImmutableTriple<String, Integer, ItemStack>> findEquippedCurio(Item item,
      final LivingEntity livingEntity) {
    return findEquippedCurio((stack) -> stack.getItem() == item, livingEntity);
  }

  @Override
  public Optional<ImmutableTriple<String, Integer, ItemStack>> findEquippedCurio(
      Predicate<ItemStack> filter, final LivingEntity livingEntity) {

    ImmutableTriple<String, Integer, ItemStack> result = getCuriosHandler(livingEntity)
        .map(handler -> {
          Map<String, ICurioStacksHandler> curios = handler.getCurios();

          for (String id : curios.keySet()) {
            ICurioStacksHandler stacksHandler = curios.get(id);
            IDynamicStackHandler stackHandler = stacksHandler.getStacks();

            for (int i = 0; i < stackHandler.size(); i++) {
              ItemStack stack = stackHandler.getStack(i);

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
  public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(String identifier,
      ItemStack stack) {
    Multimap<EntityAttribute, EntityAttributeModifier> multimap;

    if (stack.getTag() != null && stack.getTag().contains("CurioAttributeModifiers", 9)) {
      multimap = HashMultimap.create();
      ListTag listnbt = stack.getTag().getList("CurioAttributeModifiers", 10);

      for (int i = 0; i < listnbt.size(); ++i) {
        CompoundTag compoundnbt = listnbt.getCompound(i);

        if (!compoundnbt.contains("Slot", 8) || compoundnbt.getString("Slot").equals(identifier)) {
          EntityAttribute attribute = Registry.ATTRIBUTE
              .get(Identifier.tryParse(compoundnbt.getString("AttributeName")));

          if (attribute != null) {
            EntityAttributeModifier attributemodifier = EntityAttributeModifier
                .fromTag(compoundnbt);

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
    return getCurio(stack).map(curio -> curio.getAttributeModifiers(identifier))
        .orElse(HashMultimap.create());
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