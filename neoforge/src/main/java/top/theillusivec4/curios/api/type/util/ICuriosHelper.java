/*
 * Copyright (c) 2018-2023 C4
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
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.ApiStatus;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

@Deprecated(since = "1.20.1", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "1.22")
public interface ICuriosHelper {

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#getCurio(ItemStack)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  Optional<ICurio> getCurio(ItemStack stack);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#getCuriosInventory(LivingEntity)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  Optional<ICuriosItemHandler> getCuriosHandler(LivingEntity livingEntity);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#getItemStackSlots(ItemStack)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  Set<String> getCurioTags(Item item);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#getEquippedCurios()}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  Optional<IItemHandlerModifiable> getEquippedCurios(LivingEntity livingEntity);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#setEquippedCurio(String, int, ItemStack)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  void setEquippedCurio(@Nonnull LivingEntity livingEntity, String identifier, int index,
                        ItemStack stack);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#findFirstCurio(Item)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  Optional<SlotResult> findFirstCurio(@Nonnull LivingEntity livingEntity, Item item);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#findFirstCurio(Predicate)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  Optional<SlotResult> findFirstCurio(@Nonnull LivingEntity livingEntity,
                                      Predicate<ItemStack> filter);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#findCurios(Item)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  List<SlotResult> findCurios(@Nonnull LivingEntity livingEntity, Item item);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#findCurios(Predicate)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  List<SlotResult> findCurios(@Nonnull LivingEntity livingEntity, Predicate<ItemStack> filter);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#findCurios(String...)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  List<SlotResult> findCurios(@Nonnull LivingEntity livingEntity, String... identifiers);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#findCurio(String, int)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  Optional<SlotResult> findCurio(@Nonnull LivingEntity livingEntity, String identifier, int index);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#getAttributeModifiers(SlotContext, UUID, ItemStack)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid,
                                                               ItemStack stack);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#addSlotModifier(Multimap, String, UUID, double, AttributeModifier.Operation)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  void addSlotModifier(Multimap<Attribute, AttributeModifier> map, String identifier, UUID uuid,
                       double amount, AttributeModifier.Operation operation);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#addSlotModifier(ItemStack, String, String, UUID, double, AttributeModifier.Operation, String)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  void addSlotModifier(ItemStack stack, String identifier, String name, UUID uuid, double amount,
                       AttributeModifier.Operation operation, String slot);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#addModifier(ItemStack, Attribute, String, UUID, double, AttributeModifier.Operation, String)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  void addModifier(ItemStack stack, Attribute attribute, String name, UUID uuid, double amount,
                   AttributeModifier.Operation operation, String slot);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#isStackValid(SlotContext, ItemStack)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  boolean isStackValid(SlotContext slotContext, ItemStack stack);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#findFirstCurio(Item)}
   */
  @Nonnull
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  Optional<ImmutableTriple<String, Integer, ItemStack>> findEquippedCurio(Item item,
                                                                          @Nonnull
                                                                          LivingEntity livingEntity);

  /**
   * @deprecated Use {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#findFirstCurio(Predicate)}
   */
  @Nonnull
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  Optional<ImmutableTriple<String, Integer, ItemStack>> findEquippedCurio(
      Predicate<ItemStack> filter, @Nonnull LivingEntity livingEntity);


  /**
   * @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#broadcastCurioBreakEvent(SlotContext)}
   */
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  void onBrokenCurio(String id, int index, LivingEntity damager);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#broadcastCurioBreakEvent(SlotContext)}
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  void onBrokenCurio(SlotContext slotContext);

  /**
   * @deprecated Moved to internal code and removed from the API
   */
  @Deprecated(since = "1.20.1", forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  void setBrokenCurioConsumer(Consumer<SlotContext> consumer);

  /**
   * @deprecated Moved to internal code and removed from the API
   */
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  void setBrokenCurioConsumer(TriConsumer<String, Integer, LivingEntity> consumer);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.CuriosApi#getAttributeModifiers(SlotContext, UUID, ItemStack)}
   */
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
  Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier, ItemStack stack);
}
