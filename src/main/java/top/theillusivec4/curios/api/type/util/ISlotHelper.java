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
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public interface ISlotHelper {

  /**
   * Registers a {@link ISlotType} instance.
   * <br>
   * Modders: DO NOT USE DIRECTLY - Use IMC to send the appropriate {@link
   * top.theillusivec4.curios.api.SlotTypeMessage}
   *
   * @param slotType The {@link ISlotType} instance
   */
  void addSlotType(ISlotType slotType);

  /**
   * Gets the {@link ISlotType} registered to the given identifier, or {@link Optional#empty()} if
   * none is registered.
   *
   * @param identifier The {@link ISlotType} identifier
   * @return The {@link ISlotType} registered to the identifier
   */
  Optional<ISlotType> getSlotType(String identifier);

  /**
   * @return A collection of all registered {@link ISlotType}
   */
  Collection<ISlotType> getSlotTypes();

  /**
   * @return A collection of all registered {@link ISlotType} for a specific entity
   */
  Collection<ISlotType> getSlotTypes(LivingEntity livingEntity);

  /**
   * Gets all unique registered {@link ISlotType} identifiers.
   *
   * @return A set of identifiers
   */
  Set<String> getSlotTypeIds();

  /**
   * Retrieves the number of slots that an entity has for a specific curio type.
   *
   * @param livingEntity The holder of the slot(s) as a {@link LivingEntity}
   * @param id           The identifier of the {@link ISlotType}
   * @return The number of slots
   */
  int getSlotsForType(LivingEntity livingEntity, String id);

  /**
   * Sets the number of slots that an entity has for a specific curio type.
   *
   * @param id           The identifier of the {@link ISlotType}
   * @param livingEntity The holder of the slot(s) as a {@link LivingEntity}
   * @param amount       The number of slots
   */
  void setSlotsForType(String id, LivingEntity livingEntity, int amount);

  // ============ DEPRECATED ================

  /**
   * @return A map sorted by {@link ISlotType} with instances of {@link ICurioStacksHandler} using
   * default settings
   * @deprecated Use {@link ISlotHelper#getSlotTypes(LivingEntity)}
   */
  @Deprecated
  SortedMap<ISlotType, ICurioStacksHandler> createSlots(LivingEntity livingEntity);

  /**
   * @return A map sorted by {@link ISlotType} with instances of {@link ICurioStacksHandler} using
   * default settings
   * @deprecated Use {@link ISlotHelper#getSlotTypes()}
   */
  @Deprecated
  SortedMap<ISlotType, ICurioStacksHandler> createSlots();

  /**
   * @param id           The identifier of the {@link ISlotType}
   * @param livingEntity The holder of the slot(s) as a {@link LivingEntity}
   * @deprecated Add a slot modifier instead using {@link top.theillusivec4.curios.api.type.util.ICuriosHelper#addSlotModifier(Multimap, String, UUID, double, AttributeModifier.Operation)}
   * when overriding {@link top.theillusivec4.curios.api.type.capability.ICurio#getAttributeModifiers(SlotContext, UUID)}
   * <br>
   * Adds a single slot to the {@link ISlotType} with the associated identifier. If the slot to
   * be added is for a type that is not enabled on the entity, it will not be added. For adding
   * slot(s) for types that are not yet available, there must first be a call to {@link
   * ISlotHelper#unlockSlotType(String, LivingEntity)}
   */
  @Deprecated
  void growSlotType(String id, LivingEntity livingEntity);

  /**
   * @param id           The identifier of the {@link ISlotType}
   * @param amount       The number of slots to add
   * @param livingEntity The holder of the slot(s) as a {@link LivingEntity}
   * @deprecated Add a slot modifier instead using {@link top.theillusivec4.curios.api.type.util.ICuriosHelper#addSlotModifier(Multimap, String, UUID, double, AttributeModifier.Operation)}
   * when overriding {@link top.theillusivec4.curios.api.type.capability.ICurio#getAttributeModifiers(SlotContext, UUID)}
   * <br>
   * Adds multiple slots to the {@link ISlotType} with the associated identifier. If the slot to be
   * added is for a type that is not enabled on the entity, it will not be added. For adding slot(s)
   * for types that are not yet available, there must first be a call to {@link
   * ISlotHelper#unlockSlotType(String, LivingEntity)}
   */
  @Deprecated
  void growSlotType(String id, int amount, LivingEntity livingEntity);

  /**
   * @param id           The identifier of the {@link ISlotType}
   * @param livingEntity The holder of the slot(s) as a {@link LivingEntity}
   * @deprecated Add a slot modifier instead using {@link top.theillusivec4.curios.api.type.util.ICuriosHelper#addSlotModifier(Multimap, String, UUID, double, AttributeModifier.Operation)}
   * when overriding {@link top.theillusivec4.curios.api.type.capability.ICurio#getAttributeModifiers(SlotContext, UUID)}
   * <br>
   * Removes a single slot to the {@link ISlotType} with the associated identifier. If the slot to
   * be removed is the last slot available, it will not be removed. For the removal of the last
   * slot, please see {@link ISlotHelper#lockSlotType(String, LivingEntity)}
   */
  @Deprecated
  void shrinkSlotType(String id, LivingEntity livingEntity);

  /**
   * @param id           The identifier of the {@link ISlotType}
   * @param livingEntity The holder of the slot(s) as a {@link LivingEntity}
   * @deprecated Add a slot modifier instead using {@link top.theillusivec4.curios.api.type.util.ICuriosHelper#addSlotModifier(Multimap, String, UUID, double, AttributeModifier.Operation)}
   * when overriding {@link top.theillusivec4.curios.api.type.capability.ICurio#getAttributeModifiers(SlotContext, UUID)}
   * <br>
   * Removes multiple slots from the {@link ISlotType} with the associated identifier. If the slot
   * to be removed is the last slot available, it will not be removed. For the removal of the last
   * slot, please see {@link ISlotHelper#lockSlotType(String, LivingEntity)}
   */
  @Deprecated
  void shrinkSlotType(String id, int amount, LivingEntity livingEntity);

  /**
   * @deprecated Add a slot modifier instead using {@link top.theillusivec4.curios.api.type.util.ICuriosHelper#addSlotModifier(Multimap, String, UUID, double, AttributeModifier.Operation)}
   * when overriding {@link top.theillusivec4.curios.api.type.capability.ICurio#getAttributeModifiers(SlotContext, UUID)}
   */
  @Deprecated
  void unlockSlotType(String id, LivingEntity livingEntity);

  /**
   * @deprecated Add a slot modifier instead using {@link top.theillusivec4.curios.api.type.util.ICuriosHelper#addSlotModifier(Multimap, String, UUID, double, AttributeModifier.Operation)}
   * when overriding {@link top.theillusivec4.curios.api.type.capability.ICurio#getAttributeModifiers(SlotContext, UUID)}
   */
  @Deprecated
  void lockSlotType(String id, final LivingEntity livingEntity);
}
