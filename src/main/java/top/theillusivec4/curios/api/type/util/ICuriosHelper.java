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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.util.TriConsumer;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public interface ICuriosHelper {

  /**
   * Gets a {@link LazyOptional} of the curio capability attached to the {@link ItemStack}.
   *
   * @param stack The {@link ItemStack} to get the curio capability from
   * @return {@link LazyOptional} of the curio capability
   */
  LazyOptional<ICurio> getCurio(ItemStack stack);

  /**
   * Gets a {@link LazyOptional} of the curio inventory capability attached to the entity.
   *
   * @param livingEntity The {@link LivingEntity} to get the curio inventory capability from
   * @return {@link LazyOptional} of the curio inventory capability
   */
  LazyOptional<ICuriosItemHandler> getCuriosHandler(LivingEntity livingEntity);

  /**
   * Retrieves a set of string identifiers from the curio tags associated with the given item.
   *
   * @param item The {@link Item} to retrieve curio tags for
   * @return Set of unique curio identifiers associated with the item
   */
  Set<String> getCurioTags(Item item);

  /**
   * Gets a {@link LazyOptional} of an {@link IItemHandlerModifiable} that contains all the
   * equipped curio stacks (not including cosmetics).
   *
   * @param livingEntity The wearer of the curios
   * @return The equipped curio stacks, or empty if there is no curios handler
   */
  LazyOptional<IItemHandlerModifiable> getEquippedCurios(LivingEntity livingEntity);

  /**
   * Replaces the currently equipped item in a specified curio slot, if it exists.
   *
   * @param livingEntity The wearer of the curio
   * @param identifier   The identifier of the curio slot
   * @param index        The index of the curio slot
   * @param stack        The new stack to place into the slot
   */
  void setEquippedCurio(@Nonnull LivingEntity livingEntity, String identifier, int index,
                        ItemStack stack);

  /**
   * Gets the first matching item equipped in a curio slot.
   *
   * @param livingEntity The wearer as a {@link LivingEntity} of the item to be found
   * @param item         The item to search for
   * @return An optional {@link SlotResult} with the found item, or empty if none were found
   */
  Optional<SlotResult> findFirstCurio(@Nonnull LivingEntity livingEntity, Item item);

  /**
   * Gets the first matching item equipped in a curio slot that matches the filter.
   *
   * @param livingEntity The wearer as a {@link LivingEntity} of the item to be found
   * @param filter       The filter to test against
   * @return An optional {@link SlotResult} with the found item, or empty if none were found
   */
  Optional<SlotResult> findFirstCurio(@Nonnull LivingEntity livingEntity,
                                      Predicate<ItemStack> filter);

  /**
   * Gets all matching items equipped in a curio slot.
   *
   * @param livingEntity The wearer as a {@link LivingEntity} of the item to be found
   * @param item         The item to search for
   * @return A list of matching results
   */
  List<SlotResult> findCurios(@Nonnull LivingEntity livingEntity, Item item);

  /**
   * Gets all matching items equipped in a curio slot that matches the filter.
   *
   * @param livingEntity The wearer as a {@link LivingEntity} of the item to be found
   * @param filter       The filter to test against
   * @return A list of matching results
   */
  List<SlotResult> findCurios(@Nonnull LivingEntity livingEntity, Predicate<ItemStack> filter);

  /**
   * Gets all items equipped in all curio slots with specific identifiers.
   *
   * @param livingEntity The wearer as a {@link LivingEntity} of the item to be found
   * @param identifiers  The identifiers for the slot types
   * @return A list of matching results
   */
  List<SlotResult> findCurios(@Nonnull LivingEntity livingEntity, String... identifiers);

  /**
   * Gets the currently equipped item in a specified curio slot, if it exists.
   *
   * @param livingEntity The wearer of the curio
   * @param identifier   The identifier of the curio slot
   * @param index        The index of the curio slot
   * @return The equipped curio stack, or empty if there is none
   */
  Optional<SlotResult> findCurio(@Nonnull LivingEntity livingEntity, String identifier, int index);

  /**
   * Retrieves a map of attribute modifiers for the ItemStack.
   * <br>
   * Note that only the identifier is guaranteed to be present in the slot context. For instances
   * where the ItemStack may not be in a curio slot, such as when retrieving item tooltips, the
   * index is -1 and the wearer may be null.
   *
   * @param slotContext Context about the slot that the ItemStack is equipped in or may potentially
   *                    be equipped in
   * @param uuid        Slot-unique UUID
   * @param stack       The ItemStack in question
   * @return A map of attribute modifiers
   */
  Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid,
                                                               ItemStack stack);

  /**
   * Adds a slot modifier to a specified attribute map.
   *
   * @param map        A {@link Multimap} of attributes to attribute modifiers
   * @param identifier The identifier of the slot to add the modifier onto
   * @param uuid       A UUID associated wth the slot
   * @param amount     The amount of the modifier
   * @param operation  The operation of the modifier
   */
  void addSlotModifier(Multimap<Attribute, AttributeModifier> map, String identifier, UUID uuid,
                       double amount, AttributeModifier.Operation operation);

  /**
   * Adds a slot modifier to an ItemStack's tag data.
   *
   * @param stack      The ItemStack to add the modifier to
   * @param identifier The identifier of the slot to add the modifier onto
   * @param name       The name for the modifier
   * @param uuid       A UUID associated wth the modifier, or null if the slot UUID should be used
   * @param amount     The amount of the modifier
   * @param operation  The operation of the modifier
   * @param slot       The slot that the ItemStack provides the modifier from
   */
  void addSlotModifier(ItemStack stack, String identifier, String name, UUID uuid, double amount,
                       AttributeModifier.Operation operation, String slot);

  /**
   * Adds an attribute modifier to an ItemStack's tag data.
   *
   * @param stack     The ItemStack to add the modifier to
   * @param attribute The attribute to add the modifier onto
   * @param name      The name for the modifier
   * @param uuid      A UUID associated wth the modifier, or null if the slot UUID should be used
   * @param amount    The amount of the modifier
   * @param operation The operation of the modifier
   * @param slot      The slot that the ItemStack provides the modifier from
   */
  void addModifier(ItemStack stack, Attribute attribute, String name, UUID uuid, double amount,
                   AttributeModifier.Operation operation, String slot);

  /**
   * Checks if the ItemStack is valid for a particular stack and slot context.
   *
   * @param slotContext Context about the slot that the ItemStack is being checked for
   * @param stack       The ItemStack in question
   * @return True if the ItemStack is valid for the slot, false otherwise
   */
  boolean isStackValid(SlotContext slotContext, ItemStack stack);

  /**
   * Performs breaking behavior used from the single-input consumer in {@link ItemStack#hurtAndBreak(int, LivingEntity, Consumer)}
   * <br>
   * This will be necessary in order to trigger break animations in curio slots
   * <br>
   * Example: { stack.damageItem(amount, entity, damager -> CuriosApi.getCuriosHelper().onBrokenCurio(slotContext)); }
   *
   * @param slotContext Context about the slot that the curio is in
   */
  void onBrokenCurio(SlotContext slotContext);

  /**
   * Sets the {@link Consumer} that should be used with {@link ItemStack#hurtAndBreak(int,
   * LivingEntity, Consumer)} when triggering break animations in curio slots
   *
   * @param consumer The {@link Consumer} taking a {@link SlotContext}
   */
  void setBrokenCurioConsumer(Consumer<SlotContext> consumer);

  // ========= DEPRECATED =============

  /**
   * @param item         The {@link Item} to find
   * @param livingEntity The wearer as a {@link LivingEntity} of the item to be found
   * @return An {@link Optional} wrapper of the found triplet, or {@link Optional#empty()} is
   * nothing was found.
   * @deprecated Use {@link ICuriosHelper#findFirstCurio(LivingEntity, Item)}
   * <p>
   * Gets the first found {@link ItemStack} of the {@link Item} type equipped in a curio slot, or
   * {@link Optional#empty()} if no matches were found.
   */
  @Nonnull
  @Deprecated
  Optional<ImmutableTriple<String, Integer, ItemStack>> findEquippedCurio(Item item,
                                                                          @Nonnull
                                                                              LivingEntity livingEntity);

  /**
   * @param filter       The filter to test against
   * @param livingEntity The wearer as a {@link LivingEntity} of the item to be found
   * @return An {@link Optional#empty()} wrapper of the found triplet, or {@link Optional#empty()}
   * is nothing was found.
   * @deprecated Use {@link ICuriosHelper#findFirstCurio(LivingEntity, Predicate)}
   * <p>
   * Gets the first found {@link ItemStack} of the item type equipped in a curio slot that matches
   * the filter, or {@link Optional#empty()} if no matches were found.
   */
  @Nonnull
  @Deprecated
  Optional<ImmutableTriple<String, Integer, ItemStack>> findEquippedCurio(
      Predicate<ItemStack> filter, @Nonnull LivingEntity livingEntity);


  /**
   * @deprecated See {@link ICuriosHelper#onBrokenCurio(SlotContext)} for a slot context-sensitive
   * alternative
   */
  @Deprecated
  void onBrokenCurio(String id, int index, LivingEntity damager);

  /**
   * @deprecated See {@link ICuriosHelper#setBrokenCurioConsumer(Consumer)} for a slot
   * context-sensitive alternative
   */
  @Deprecated
  void setBrokenCurioConsumer(TriConsumer<String, Integer, LivingEntity> consumer);

  /**
   * @deprecated See {@link ICuriosHelper#getAttributeModifiers(SlotContext, UUID, ItemStack)} for
   * an alternative method with additional context and a slot-unique UUID.
   */
  @Deprecated
  Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier, ItemStack stack);
}
