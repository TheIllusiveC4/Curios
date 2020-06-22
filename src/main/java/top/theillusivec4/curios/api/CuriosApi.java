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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.util.TriConsumer;
import top.theillusivec4.curios.api.imc.CurioImcMessage;
import top.theillusivec4.curios.api.type.ICurio;
import top.theillusivec4.curios.api.type.ICurioItemHandler;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.SlotType;

public final class CuriosApi {

  /**
   * Holds a reference to the Curios modid.
   */
  public static final String MODID = "curios";
  /**
   * The maps containing the CurioType and icons with identifiers as keys Try not to access these
   * directly and instead use {@link CuriosApi#getType(String)} and {@link
   * CuriosApi#getIcon(String)}.
   * <br>DO NOT REGISTER DIRECTLY - Use IMC to send the appropriate {@link
   * CurioImcMessage}
   */
  public static Map<String, ISlotType> idToType = new HashMap<>();
  public static Map<String, ResourceLocation> idToIcon = new HashMap<>();
  public static TriConsumer<String, Integer, LivingEntity> brokenCurioConsumer;

  /**
   * Gets the LazyOptional of the curio capability attached to the ItemStack.
   *
   * @param stack The ItemStack to get the curio capability from
   * @return LazyOptional of the curio capability
   */
  public static LazyOptional<ICurio> getCurio(ItemStack stack) {
    return stack.getCapability(CuriosCapability.ITEM);
  }

  /**
   * Gets the LazyOptional of the curio inventory capability attached to the entity.
   *
   * @param livingEntity The ItemStack to get the curio inventory capability from
   * @return LazyOptional of the curio inventory capability
   */
  public static LazyOptional<ICurioItemHandler> getCuriosHandler(
      @Nonnull final LivingEntity livingEntity) {
    return livingEntity.getCapability(CuriosCapability.INVENTORY);
  }

  public static Collection<ISlotType> getTypes() {
    return Collections.unmodifiableCollection(idToType.values());
  }

  /**
   * Passes three inputs into an internal triple-input consumer that should be used from the
   * single-input consumer in {@link ItemStack#damageItem(int, LivingEntity, Consumer)}
   * <br>
   * This will be necessary in order to trigger break animations in curio slots
   * <br>
   * Example: { stack.damageItem(amount, entity, damager -> CuriosAPI.onBrokenCurio(id, index,
   * damager)); }
   *
   * @param id      The {@link SlotType} String identifier
   * @param index   The slot index of the identifier
   * @param damager The entity that is breaking the item
   */
  public static void onBrokenCurio(String id, int index, LivingEntity damager) {
    brokenCurioConsumer.accept(id, index, damager);
  }

  /**
   * Gets the Optional wrapper of the CurioType from the given identifier.
   *
   * @param identifier The unique identifier for the {@link ISlotType}
   * @return Optional wrapper of the CurioType  or Optional.empty() if not present
   */
  public static Optional<ISlotType> getType(String identifier) {
    return Optional.ofNullable(idToType.get(identifier));
  }

  /**
   * Gets an unmodifiable list of all unique registered identifiers.
   *
   * @return A list of identifiers
   */
  public static Set<String> getTypeIdentifiers() {
    return Collections.unmodifiableSet(idToType.keySet());
  }

  /**
   * Retrieves the number of slots that an entity has for a specific curio type.
   *
   * @param livingEntity The entity that has the slot
   * @param identifier   The type identifier of the slot
   * @return The number of slots
   */
  public static int getSlotsForType(@Nonnull final LivingEntity livingEntity, String identifier) {
    return CuriosApi.getCuriosHandler(livingEntity).map(
        handler -> handler.getStacksHandler(identifier).map(ICurioStacksHandler::getSlots)
            .orElse(0)).orElse(0);
  }

  /**
   * Sets the number of slots that an entity has for a specific curio type.
   *
   * @param livingEntity The entity that has the slot
   * @param id           The type identifier of the slot
   * @param amount       The number of slots
   */
  public static void setSlotsForType(String id, final LivingEntity livingEntity, int amount) {
    int difference = amount - CuriosApi.getSlotsForType(livingEntity, id);

    if (difference > 0) {
      CuriosApi.growSlotType(id, difference, livingEntity);
    } else if (difference < 0) {
      CuriosApi.shrinkSlotType(id, Math.abs(difference), livingEntity);
    }
  }

  /**
   * Gets the first found ItemStack of the item type equipped in a curio slot, or null if no matches
   * were found.
   *
   * @param item         The item to find
   * @param livingEntity The wearer of the item to be found
   * @return An Optional wrapper of the found triplet, or Optional.empty() is nothing was found.
   */
  public static Optional<ImmutableTriple<String, Integer, ItemStack>> getCurioEquipped(Item item,
      @Nonnull final LivingEntity livingEntity) {
    return getCurioEquipped((stack) -> stack.getItem() == item, livingEntity);
  }

  /**
   * Gets the first found ItemStack of the item type equipped in a curio slot that matches the
   * filter, or null if no matches were found.
   *
   * @param filter       The filter to test the ItemStack against
   * @param livingEntity The wearer of the item to be found
   * @return An Optional wrapper of the found triplet, or Optional.empty() is nothing was found.
   */
  @Nonnull
  public static Optional<ImmutableTriple<String, Integer, ItemStack>> getCurioEquipped(
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

  public static Multimap<String, AttributeModifier> getAttributeModifiers(String identifier,
      ItemStack stack) {
    Multimap<String, AttributeModifier> multimap;

    if (stack.getTag() != null && stack.getTag().contains("CurioAttributeModifiers", 9)) {
      multimap = HashMultimap.create();
      ListNBT listnbt = stack.getTag().getList("CurioAttributeModifiers", 10);

      for (int i = 0; i < listnbt.size(); ++i) {
        CompoundNBT compoundnbt = listnbt.getCompound(i);
        AttributeModifier attributemodifier = SharedMonsterAttributes
            .readAttributeModifier(compoundnbt);

        if (attributemodifier != null && (!compoundnbt.contains("Slot", 8) || compoundnbt
            .getString("Slot").equals(identifier))
            && attributemodifier.getID().getLeastSignificantBits() != 0L
            && attributemodifier.getID().getMostSignificantBits() != 0L) {
          multimap.put(compoundnbt.getString("AttributeName"), attributemodifier);
        }
      }
      return multimap;
    }
    return getCurio(stack).map(curio -> curio.getAttributeModifiers(identifier))
        .orElse(HashMultimap.create());
  }

  /**
   * /** Adds a single slot to the {@link SlotType} with the associated identifier. If the slot to
   * be added is for a type that is not enabled on the entity, it will not be added. For adding
   * slot(s) for types that are not yet available, there must first be a call to {@link
   * CuriosApi#unlockSlotType(String, LivingEntity)}
   *
   * @param id               The identifier of the CurioType
   * @param entityLivingBase The holder of the slot(s)
   */
  public static void growSlotType(String id, final LivingEntity entityLivingBase) {
    growSlotType(id, 1, entityLivingBase);
  }

  /**
   * Adds multiple slots to the {@link SlotType} with the associated identifier. If the slot to be
   * added is for a type that is not enabled on the entity, it will not be added. For adding slot(s)
   * for types that are not yet available, there must first be a call to {@link
   * CuriosApi#unlockSlotType(String, LivingEntity)}
   *
   * @param id               The identifier of the CurioType
   * @param amount           The number of slots to add
   * @param entityLivingBase The holder of the slots
   */
  public static void growSlotType(String id, int amount, final LivingEntity entityLivingBase) {
    getCuriosHandler(entityLivingBase).ifPresent(handler -> handler.growSlotType(id, amount));
  }

  /**
   * Removes a single slot to the {@link SlotType} with the associated identifier. If the slot to be
   * removed is the last slot available, it will not be removed. For the removal of the last slot,
   * please see {@link CuriosApi#lockSlotType(String, LivingEntity)}
   *
   * @param id               The identifier of the CurioType
   * @param entityLivingBase The holder of the slot(s)
   */
  public static void shrinkSlotType(String id, final LivingEntity entityLivingBase) {
    shrinkSlotType(id, 1, entityLivingBase);
  }

  /**
   * Removes multiple slots to the {@link SlotType} with the associated identifier. If the slot to
   * be removed is the last slot available, it will not be removed. For the removal of the last
   * slot, please see {@link CuriosApi#lockSlotType(String, LivingEntity)}
   *
   * @param id               The identifier of the CurioType
   * @param entityLivingBase The holder of the slot(s)
   */
  public static void shrinkSlotType(String id, int amount, final LivingEntity entityLivingBase) {
    getCuriosHandler(entityLivingBase).ifPresent(handler -> handler.shrinkSlotType(id, amount));
  }

  /**
   * Adds a {@link SlotType} to the entity The number of slots given is the type's default.
   *
   * @param id               The identifier of the CurioType
   * @param entityLivingBase The holder of the slot(s)
   */
  public static void unlockSlotType(String id, final LivingEntity entityLivingBase) {
    getCuriosHandler(entityLivingBase).ifPresent(handler -> handler.unlockSlotType(id));
  }

  /**
   * Removes a {@link SlotType} from the entity.
   *
   * @param id               The identifier of the CurioType
   * @param entityLivingBase The holder of the slot(s)
   */
  public static void lockSlotType(String id, final LivingEntity entityLivingBase) {
    getCuriosHandler(entityLivingBase).ifPresent(handler -> handler.lockSlotType(id));
  }

  /**
   * Retrieves a set of string identifiers from the curio tags associated with the given item.
   *
   * @param item The item to retrieve curio tags for
   * @return Unmodifiable list of unique curio identifiers associated with the item
   */
  public static Set<String> getCurioTags(Item item) {
    return item.getTags().stream().filter(tag -> tag.getNamespace().equals(MODID))
        .map(ResourceLocation::getPath).collect(Collectors.toSet());
  }

  /**
   * @return An unmodifiable map of identifiers and their registered icons.
   */
  @Nonnull
  public static ResourceLocation getIcon(String identifier) {
    return idToIcon
        .getOrDefault(identifier, new ResourceLocation("item/empty_" + identifier + "_slot"));
  }

  /**
   * Holder class for IMC message identifiers.
   */
  public static final class Imc {

    public static final String REGISTER_TYPE = "register_type";
    public static final String MODIFY_TYPE = "modify_type";
  }
}
