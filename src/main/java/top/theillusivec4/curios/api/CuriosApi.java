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

package top.theillusivec4.curios.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.util.ICuriosHelper;
import top.theillusivec4.curios.api.type.util.IIconHelper;
import top.theillusivec4.curios.api.type.util.ISlotHelper;

public final class CuriosApi {

  private static final Logger LOGGER = LogUtils.getLogger();

  public static final String MODID = "curios";

  public static Optional<ISlotType> getSlot(String id) {
    apiError();
    return Optional.empty();
  }

  @Nonnull
  public static ResourceLocation getSlotIcon(String id) {
    apiError();
    return new ResourceLocation(MODID, "slot/empty_curio_slot");
  }

  public static Map<String, ISlotType> getSlots() {
    apiError();
    return new HashMap<>();
  }

  /**
   * @return The slot types provided to player entities
   */
  public static Map<String, ISlotType> getPlayerSlots() {
    apiError();
    return new HashMap<>();
  }

  /**
   * @return The slot types provided to the given entity type
   */
  public static Map<String, ISlotType> getEntitySlots(EntityType<?> type) {
    apiError();
    return new HashMap<>();
  }

  public static Map<String, ISlotType> getItemStackSlots(ItemStack stack) {
    apiError();
    return new HashMap<>();
  }

  public static Map<String, ISlotType> getItemStackSlots(ItemStack stack,
                                                         LivingEntity livingEntity) {
    apiError();
    return new HashMap<>();
  }

  public static LazyOptional<ICurio> getCurio(ItemStack stack) {
    apiError();
    return LazyOptional.empty();
  }

  public static LazyOptional<ICuriosItemHandler> getCuriosInventory(LivingEntity livingEntity) {
    apiError();
    return LazyOptional.empty();
  }

  /**
   * Checks if the ItemStack is valid for a particular stack and slot context.
   *
   * @param slotContext Context about the slot that the ItemStack is being checked for
   * @param stack       The ItemStack in question
   * @return True if the ItemStack is valid for the slot, false otherwise
   */
  public static boolean isStackValid(SlotContext slotContext, ItemStack stack) {
    apiError();
    return false;
  }

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
  public static Multimap<Attribute, AttributeModifier> getAttributeModifiers(
      SlotContext slotContext, UUID uuid, ItemStack stack) {
    apiError();
    return HashMultimap.create();
  }

  /**
   * Adds a slot modifier to a specified attribute map.
   *
   * @param map        A {@link Multimap} of attributes to attribute modifiers
   * @param identifier The identifier of the slot to add the modifier onto
   * @param uuid       A UUID associated wth the slot
   * @param amount     The amount of the modifier
   * @param operation  The operation of the modifier
   */
  public static void addSlotModifier(Multimap<Attribute, AttributeModifier> map, String identifier,
                                     UUID uuid, double amount,
                                     AttributeModifier.Operation operation) {
    apiError();
  }

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
  public static void addSlotModifier(ItemStack stack, String identifier, String name, UUID uuid,
                                     double amount, AttributeModifier.Operation operation,
                                     String slot) {
    apiError();
  }

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
  public static void addModifier(ItemStack stack, Attribute attribute, String name, UUID uuid,
                                 double amount, AttributeModifier.Operation operation,
                                 String slot) {
    apiError();
  }

  /**
   * Performs breaking behavior used from the single-input consumer in {@link ItemStack#hurtAndBreak(int, LivingEntity, Consumer)}
   * <br>
   * This will be necessary in order to trigger break animations in curio slots
   * <br>
   * Example: { stack.hurtAndBreak(amount, entity, damager -> CuriosApi.broadcastCurioBreakEvent(slotContext)); }
   *
   * @param slotContext Context about the slot that the curio is in
   */
  public static void broadcastCurioBreakEvent(SlotContext slotContext) {
    apiError();
  }

  private static void apiError() {
    LOGGER.error("Missing Curios API implementation!");
  }

  // ========= DEPRECATED =============

  private static IIconHelper iconHelper;

  @Deprecated(since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  public static void setIconHelper(IIconHelper helper) {

    if (iconHelper == null) {
      iconHelper = helper;
    }
  }

  @Deprecated(since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  public static IIconHelper getIconHelper() {
    return iconHelper;
  }

  private static ICuriosHelper curiosHelper;

  @Deprecated(since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  public static void setCuriosHelper(ICuriosHelper helper) {

    if (curiosHelper == null) {
      curiosHelper = helper;
    }
  }

  @Deprecated(since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  public static ICuriosHelper getCuriosHelper() {
    return curiosHelper;
  }

  private static ISlotHelper slotHelper;

  @Deprecated(forRemoval = true, since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  public static ISlotHelper getSlotHelper() {
    return slotHelper;
  }

  @Deprecated(forRemoval = true, since = "1.20.1")
  @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
  public static void setSlotHelper(ISlotHelper helper) {
    slotHelper = helper;
  }
}
