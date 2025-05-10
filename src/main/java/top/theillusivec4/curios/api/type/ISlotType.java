/*
 * Copyright (c) 2018-2024 C4
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
 *
 */

package top.theillusivec4.curios.api.type;

import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.function.BiPredicate;
import javax.annotation.Nullable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosResources;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.internal.CuriosServices;

/**
 * This interface provides read-only data for slot types used throughout Curios. Modders are not
 * expected to create their own implementations using this interface. All non-static methods in this
 * interface are expected to return non-null.
 */
public interface ISlotType extends Comparable<ISlotType> {

  /**
   * Gets the {@link ISlotType} instance from an identifier, or null if none are found.
   *
   * <p>This searches a string to slot type keyed map, populated on world load after datapacks are
   * loaded. The returned slot type will stay the same instance across multiple calls until
   * datapacks are (re)loaded again.
   *
   * @param id the unique identifier for a slot type.
   * @return the slot type for the identifier, or null if none are found.
   */
  @Nullable
  static ISlotType get(String id) {
    return CuriosSlotTypes.getSlotTypes().get(id);
  }

  /**
   * Gets the identifier for this slot type.
   *
   * <p>The identifier is unique to this slot type and is used to sort alphabetically when
   * {@link #getOrder()} is equal to another slot type. This is also used for localization keys,
   * in the format of `curios.identifier.x` where x is the identifier.
   *
   * @return the unique identifier for this slot type.
   */
  default String getId() {
    return this.getIdentifier();
  }

  /**
   * Gets the {@link ResourceLocation} of the image icon for this slot type.
   *
   * <p>The location is used to set the image icon for the background of the slot type when rendered
   * inside an inventory screen. However, an inventory slot that identifies as a cosmetic slot
   * will ignore this icon and instead use a separate one dedicated to that type.
   *
   * @return the location for the image icon associated with this slot type. If null or invalid,
   *     {@link ISlotType#GENERIC_ICON} will be used instead.
   */
  ResourceLocation getIcon();

  /**
   * Gets the numbered ordering priority for this slot type.
   *
   * <p>Lower numbers are considered to be higher priority, so this slot type will be encountered
   * sooner during traversal if this ordering priority is numbered lower. If another slot type is
   * of an equal number, then sorting falls back to the natural ordering of {@link #getId()}.
   *
   * @return the ordering priority of this slot type.
   */
  int getOrder();

  /**
   * Gets the number of slots to associate with this slot type by default.
   *
   * <p>The number of slots during gameplay that can be given to any given entity is dynamic. This
   * number only represents the default amount to give during initialization or reset of data.
   *
   * @return the number of slots to give by default for this slot type.
   */
  int getSize();

  /**
   * Determines if this slot type should be added to the native GUI implementation.
   *
   * <p>A slot type added to the native GUI implementation will appear alongside other slot types
   * of the same nature in the Curios inventory screen. A slot type that is omitted is expected
   * to be implemented elsewhere by a modder.
   *
   * @return true if the slot type appears in the native GUI, false otherwise.
   */
  boolean useNativeGui();

  /**
   * Determines if this slot type includes cosmetic slots.
   *
   * <p>Cosmetic slots appear in the same number as default slots, acccording to {@link #getSize()}.
   * This effectively doubles the default size of this slot type when considering both categories
   * together. Each cosmetic slot is paired with a default slot.
   *
   * <p>Cosmetic slots provide no functionality for the items held inside, but take rendering
   * priority over a default slot. In the case where items are in both, only the cosmetic slot item
   * model will be rendered on the entity. Cosmetic slots still follow the rendering toggles added
   * by {@link #canToggleRendering()}.
   *
   * @return true if the slot type has cosmetic slots, false otherwise.
   */
  boolean hasCosmetic();

  /**
   * Determines if this slot type can have their rendering toggled on and off.
   *
   * <p>A render toggle appears as a small button in the top-right corner of the Curios inventory
   * screen slot. Clicking on the button to toggle this off will cause the slot, and its cosmetic
   * slot pair if {@link #hasCosmetic()} is true, to skip rendering models on entities for any
   * items held inside.
   *
   * @return true if the slot type can toggle rendering on entities, false otherwise.
   */
  boolean canToggleRendering();

  /**
   * Gets the {@link DropRule} for this slot type.
   *
   * <p>This drop rule is called when determining the drop behavior of this slot type. This can
   * occur during player/entity death or when a game event forces an item to be dropped from the
   * curios inventory.
   *
   * <p>This drop rule can be overridden by the item inside the slot through
   * {@link top.theillusivec4.curios.api.common.DropRule} and globally through
   * {@link top.theillusivec4.curios.api.event.DropRulesEvent}.
   *
   * @return the drop rule for this slot type.
   */
  DropRule getDropRule();

  /**
   * Gets the {@link ResourceLocation} locations of validators to apply to this slot type.
   *
   * <p>Validators are used to test for item validity, in order to determine whether any given
   * ItemStack can be accepted into a slot of this slot type. This does not determine final
   * eligibility, which can be altered through
   * {@link top.theillusivec4.curios.api.type.capability.ICurioItem#canEquip(SlotContext, ItemStack)}
   * and {@link top.theillusivec4.curios.api.event.CurioCanEquipEvent}.
   *
   * <p>If empty, the validator will be accepted as the result of a predicate that tests the
   * ItemStack for the presence of a {{@code #curios:id}} item tag, where id is the returned value
   * from {@link #getId()}.
   *
   * <p>Locations are registered as predicates through
   * {@link CuriosSlotTypes#registerPredicate(ResourceLocation, BiPredicate)}.
   *
   * @return the set of locations keyed to the validator predicates on this slot type.
   */
  Set<ResourceLocation> getValidators();

  /**
   * Gets the {@link EntityType} objects to be given this slot type by default.
   *
   * <p>This slot type will be initialized as a part of the curios inventory for each type of entity
   * on this list. Due to the dynamic nature of the curios inventory, this behavior will only be
   * applied on the first initialization of the curios inventory data or on datapack reloads.
   *
   * <p>If empty, the entity types will be accepted as
   *
   * <p>Locations are registered as predicates through
   * {@link CuriosSlotTypes#registerPredicate(ResourceLocation, BiPredicate)}.
   *
   * @return the set of entity types to be given this slot type by default.
   */
  Set<EntityType<?>> getDefaultEntityTypes();

  default boolean isItemValid(SlotContext slotContext, ItemStack stack) {
    return CuriosSlotTypes.testPredicates(slotContext, stack, this.getValidators());
  }

  /**
   * The location of an image icon to use when specified image icons are null, missing, or invalid.
   */
  ResourceLocation GENERIC_ICON = CuriosResources.resource("textures/gui/generic_icon.png");

  /**
   * The codec used for (de)serializing slot type data.
   */
  Codec<ISlotType> CODEC = CuriosServices.CODECS.slotTypeCodec();

  /**
   * The stream codec used for transferring slot type networking data.
   */
  StreamCodec<RegistryFriendlyByteBuf, ISlotType> STREAM_CODEC =
      CuriosServices.CODECS.slotTypeStreamCodec();

  /**
   * Compares this slot type with the specified slot type for order. Returns a
   * negative integer, zero, or a positive integer as this slot type is less
   * than, equal to, or greater than the specified slot type.
   *
   * <p>Comparisons are first made from {@link #getOrder()} with lower values returning lower
   * integers for comparisons. A slot type with a lower order value will be considered less than
   * a slot type with a higher order value. If order values are equal, then comparisons are made
   * between the natural ordering of {@link #getId()}.
   *
   * <p>Since identifiers are unique, it is expected that
   * {{@code (x.compareTo(y)==0) == (x.equals(y))}} for a given slot type object.
   *
   * @param o the slot type to be compared.
   * @return a negative integer, zero, or a positive integer as this slot type
   *     *          is less than, equal to, or greater than the specified slot type.
   */
  @Override
  default int compareTo(@NotNull ISlotType o) {
    if (this.getOrder() == o.getOrder()) {
      return this.getId().compareTo(o.getId());
    } else if (this.getOrder() > o.getOrder()) {
      return 1;
    } else {
      return -1;
    }
  }

  // ============ DEPRECATED ==============

  /**
   * Gets the identifier for this slot type.
   *
   * <p>The identifier is unique to this slot type and is used to sort alphabetically when
   * {@link #getOrder()} is equal to another slot type. This is also used for localization keys,
   * in the format of `curios.identifier.x` where x is the identifier.
   *
   * @return the unique identifier for this slot type.
   * @see #getId()
   * @deprecated As of 1.21.4, replaced by {@link ISlotType#getId()} for a shortened alternative.
   */
  @Deprecated(since = "1.21.4")
  String getIdentifier();
}
