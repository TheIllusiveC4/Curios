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

package top.theillusivec4.curios.api.type.inventory;

import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public interface ICurioStacksHandler {

  /**
   * Gets the {@link IDynamicStackHandler} for the equipped curio stacks.
   *
   * @return The {@link IDynamicStackHandler} for the equipped curio stacks
   */
  IDynamicStackHandler getStacks();

  /**
   * Gets the {@link IDynamicStackHandler} for the equipped cosmetic curio stacks.
   * <br>
   * The size of this list should always match the sie of {@link ICurioStacksHandler#getStacks()}
   *
   * @return The {@link IDynamicStackHandler} for the equipped cosmetic curio stacks
   */
  IDynamicStackHandler getCosmeticStacks();

  /**
   * Gets a list of boolean values that represent render states. True for rendering and false for no
   * rendering.
   * <br>
   * The size of this list should always match the size of {@link ICurioStacksHandler#getStacks()}.
   *
   * @return A list of boolean values for render states
   */
  NonNullList<Boolean> getRenders();

  /**
   * Gets whether this stack handler can toggle rendering its contents on an entity, which is stored in {@link ICurioStacksHandler#getRenders()}.
   *
   * @return True to allow render toggling, false otherwise
   */
  default boolean canToggleRendering() {
    return true;
  }

  /**
   * Gets the drop rule that determines behavior for the contents upon death.
   * See {@link ICurio.DropRule} for possible values.
   * <br>
   * {@link ICurio.DropRule#DEFAULT} will defer to the drop behavior defined by the Curios
   * configuration.
   *
   * @return The {@link ICurio.DropRule} to use for drop behavior
   */
  default ICurio.DropRule getDropRule() {
    return ICurio.DropRule.DEFAULT;
  }

  /**
   * Gets the number of slots for equipped curio stacks.
   * <br>
   * This number should always match the size of {@link ICurioStacksHandler#getStacks()}
   *
   * @return The number of slots for equipped curio stacks.
   */
  int getSlots();

  /**
   * Gets whether or not this stack handler should be visible. This does not lock the stack handler
   * from being used regardless.
   *
   * @return True or false for visibility
   */
  boolean isVisible();

  /**
   * Gets whether or not this stack handler has cosmetic handling. This does not lock the cosmetic
   * stack handler from being used regardless.
   *
   * @return True or false for cosmetic handling
   */
  boolean hasCosmetic();

  /**
   * Writes the data for this handler.
   *
   * @return A {@link CompoundTag} representing the serialized data
   */
  CompoundTag serializeNBT();

  /**
   * Reads the data into this handler.
   *
   * @param nbt A {@link CompoundTag} representing the serialized data
   */
  void deserializeNBT(CompoundTag nbt);

  /**
   * Retrieves the slot identifier associated with the handler.
   *
   * @return The slot identifier
   */
  String getIdentifier();

  /**
   * Retrieves all the slot modifiers on the handler.
   *
   * @return A map of modifiers with the UUID as keys and {@link AttributeModifier} as values
   */
  Map<UUID, AttributeModifier> getModifiers();

  /**
   * Retrieves all the permanent slot modifiers on the handler.
   * <br>
   * These slot modifiers are serialized on the handler.
   *
   * @return A set of {@link AttributeModifier}
   */
  Set<AttributeModifier> getPermanentModifiers();

  /**
   * Retrieves all the transient modifiers that have been deserialized but not yet processed.
   *
   * @return A set of {@link AttributeModifier}
   */
  Set<AttributeModifier> getCachedModifiers();

  /**
   * Retrieves all the slot modifiers for a given operation on the handler.
   *
   * @param operation The operation of the modifiers
   * @return A collection of {@link AttributeModifier}
   */
  Collection<AttributeModifier> getModifiersByOperation(AttributeModifier.Operation operation);

  /**
   * Adds a temporary slot modifier to the handler.
   * <br>
   * These slot modifiers are not serialized on the handler.
   *
   * @param modifier The {@link AttributeModifier} instance to add
   */
  void addTransientModifier(AttributeModifier modifier);

  /**
   * Adds a permanent slot modifier to the handler.
   * <br>
   * These slot modifiers are serialized on the handler.
   *
   * @param modifier The {@link AttributeModifier} instance to add
   */
  void addPermanentModifier(AttributeModifier modifier);

  /**
   * Removes a slot modifier from the handler.
   *
   * @param uuid The UUID of the modifier to remove
   */
  void removeModifier(UUID uuid);

  /**
   * Removes all the slot modifiers on the handler.
   */
  void clearModifiers();

  /**
   * Removes the cached modifiers that appear upon deserialization of the handler.
   * <br>
   * Primarily for internal use, used as a workaround to avoid calculating slot stacks before slot
   * modifiers are initially applied.
   */
  void clearCachedModifiers();

  /**
   * Copies all the slot modifiers from another instance to this one.
   *
   * @param other The other instance
   */
  void copyModifiers(ICurioStacksHandler other);

  /**
   * Recalculates the slot modifiers and resizes the handler.
   */
  void update();

  /**
   * Retrieves the NBT data to sync to clients.
   *
   * @return The data represented as a {@link CompoundTag}
   */
  CompoundTag getSyncTag();

  /**
   * Applies the NBT data synced to clients.
   * <br>
   * Client-side only.
   *
   * @param tag The data represented as a {@link CompoundTag}
   */
  void applySyncTag(CompoundTag tag);

  // ============ DEPRECATED ================

  /**
   * @deprecated See {@link ICurioStacksHandler#getModifiers()}
   */
  @Deprecated
  int getSizeShift();

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#addTransientSlotModifiers(Multimap)}
   * and {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#addPermanentSlotModifiers(Multimap)}
   */
  @Deprecated
  void grow(int amount);

  /**
   * @deprecated See {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#addTransientSlotModifiers(Multimap)}
   * and {@link top.theillusivec4.curios.api.type.capability.ICuriosItemHandler#addPermanentSlotModifiers(Multimap)}
   */
  @Deprecated
  void shrink(int amount);
}
