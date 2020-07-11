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

package top.theillusivec4.curios.api.type.util;

import com.google.common.collect.Multimap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.util.TriConsumer;
import top.theillusivec4.curios.api.type.component.ICurio;
import top.theillusivec4.curios.api.type.component.ICuriosItemHandler;

public interface ICuriosHelper {

  /**
   * Gets a {@link Optional} of the curio capability attached to the {@link ItemStack}.
   *
   * @param stack The {@link ItemStack} to get the curio capability from
   * @return {@link Optional} of the curio capability
   */
  Optional<ICurio> getCurio(ItemStack stack);

  /**
   * Gets a {@link Optional} of the curio inventory capability attached to the entity.
   *
   * @param livingEntity The {@link LivingEntity} to get the curio inventory capability from
   * @return {@link Optional} of the curio inventory capability
   */
  Optional<ICuriosItemHandler> getCuriosHandler(LivingEntity livingEntity);

  /**
   * Retrieves a set of string identifiers from the curio tags associated with the given item.
   *
   * @param item The {@link Item} to retrieve curio tags for
   * @return Set of unique curio identifiers associated with the item
   */
  Set<String> getCurioTags(Item item);

  /**
   * Gets the first found {@link ItemStack} of the {@link Item} type equipped in a curio slot, or
   * {@link Optional#empty()} if no matches were found.
   *
   * @param item         The {@link Item} to find
   * @param livingEntity The wearer as a {@link LivingEntity} of the item to be found
   * @return An {@link Optional} wrapper of the found triplet, or {@link Optional#empty()} is
   * nothing was found.
   */
  Optional<ImmutableTriple<String, Integer, ItemStack>> findEquippedCurio(Item item,
      LivingEntity livingEntity);

  /**
   * Gets the first found {@link ItemStack} of the item type equipped in a curio slot that matches
   * the filter, or {@link Optional#empty()} if no matches were found.
   *
   * @param filter       The filter to test against
   * @param livingEntity The wearer as a {@link LivingEntity} of the item to be found
   * @return An {@link Optional#empty()} wrapper of the found triplet, or {@link Optional#empty()}
   * is nothing was found.
   */
  Optional<ImmutableTriple<String, Integer, ItemStack>> findEquippedCurio(
      Predicate<ItemStack> filter, LivingEntity livingEntity);

  Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(String identifier,
      ItemStack stack);

  /**
   * Passes three inputs into an internal triple-input consumer that should be used from the
   * single-input consumer in {@link ItemStack#damage(int, LivingEntity, Consumer)}
   * <br>
   * This will be necessary in order to trigger break animations in curio slots
   * <br>
   * Example: { stack.damageItem(amount, entity, damager -> CuriosApi.getCuriosHelper().onBrokenCurio(id,
   * index, damager)); }
   *
   * @param id      The {@link top.theillusivec4.curios.api.type.ISlotType} String identifier
   * @param index   The slot index of the identifier
   * @param damager The entity that is breaking the item
   */
  void onBrokenCurio(String id, int index, LivingEntity damager);

  /**
   * Sets the {@link TriConsumer} that should be used with {@link ItemStack#damage(int,
   * LivingEntity, Consumer)} when triggering break animations in curio slots
   *
   * @param consumer The {@link TriConsumer} taking an {@link top.theillusivec4.curios.api.type.ISlotType}
   *                 identifier, a slot index, and the wearer as a {@link LivingEntity}
   */
  void setBrokenCurioConsumer(TriConsumer<String, Integer, LivingEntity> consumer);
}
