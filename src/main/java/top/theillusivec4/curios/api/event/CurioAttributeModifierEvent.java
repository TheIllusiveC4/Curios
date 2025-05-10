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

package top.theillusivec4.curios.api.event;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.common.slot.SlotTypePredicate;
import top.theillusivec4.curios.api.type.ISlotType;

/**
 * This event is fired when the attributes for curio are queried (for any reason) through
 * {@link top.theillusivec4.curios.api.type.capability.ICurioItem#getAttributeModifiers(ItemStack)}.
 *
 * <p>This event is fired regardless of if the stack has
 * {@link top.theillusivec4.curios.api.CuriosDataComponents#ATTRIBUTE_MODIFIERS} or not. If your
 * attribute should be ignored when attributes are overridden, you can check for the presence of the
 * component.
 *
 * <p>This event may be fired on both the logical server and logical client.
 *
 * <p>This event is fired on the {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS}.
 */
public class CurioAttributeModifierEvent extends Event {

  private final ItemStack stack;
  private final CurioAttributeModifiers defaultModifiers;
  private final Multimap<Holder<Attribute>, AttributeModifier> originalMap;
  private CurioAttributeModifierEvent.CurioAttributeModifiersBuilder builder;

  @ApiStatus.Internal
  public CurioAttributeModifierEvent(ItemStack stack, CurioAttributeModifiers defaultModifiers) {
    this.stack = stack;
    this.defaultModifiers = defaultModifiers;
    Multimap<Holder<Attribute>, AttributeModifier> modifierMap = LinkedHashMultimap.create();

    for (CurioAttributeModifiers.Entry modifier : this.defaultModifiers.modifiers()) {
      modifierMap.put(modifier.attributeHolder(), modifier.modifier());
    }
    this.originalMap = modifierMap;
  }

  /**
   * Returns the ItemStack that is being computed for curio attribute modifiers.
   *
   * @return The ItemStack being computed.
   */
  public ItemStack getItemStack() {
    return this.stack;
  }

  /**
   * Returns the default curio attribute modifiers before modifications from this event.
   *
   * @return The original curio attribute modifiers.
   */
  public CurioAttributeModifiers getDefaultModifiers() {
    return this.defaultModifiers;
  }

  /**
   * Returns an unmodifiable view of the attribute modifier entries. Do not use the returned value
   * to create an {@link CurioAttributeModifiers} since the underlying list is not immutable.
   *
   * <p>If you need an {@link CurioAttributeModifiers}, you may need to call {@link #build()}.
   *
   * @apiNote Use other methods from this event to adjust the modifiers.
   */
  public List<CurioAttributeModifiers.Entry> getImmutableModifiers() {
    return this.builder == null ? this.defaultModifiers.modifiers() : this.builder.getEntryView();
  }

  /**
   * Adds a new attribute modifier to the given stack. Two modifiers with the same id may not exist
   * for the same attribute, and this method will fail if one exists.
   *
   * <p>Slot modifiers can be added by calling
   * {@link top.theillusivec4.curios.api.SlotAttribute#getOrCreate(String)} to obtain an instance of
   * the attribute holder wrapper for a slot identifier.
   *
   * @param attribute         The attribute the modifier is for.
   * @param modifier          The new attribute modifier.
   * @param slotTypePredicate The slot type predicate for which the modifier should apply.
   * @return True if the modifier was added, false if it was already present.
   * @apiNote Modifiers must have a unique and consistent {@link ResourceLocation} id, or the
   *     modifier will not be removed when the item is unequipped.
   */
  public boolean addModifier(Holder<Attribute> attribute, AttributeModifier modifier,
                             SlotTypePredicate slotTypePredicate) {
    return this.getBuilder().addModifier(attribute, modifier, slotTypePredicate);
  }

  /**
   * Adds a new attribute modifier to the given stack. Two modifiers with the same id may not exist
   * for the same attribute, and this method will fail if one exists.
   *
   * <p>Slot modifiers can be added by calling
   * {@link top.theillusivec4.curios.api.SlotAttribute#getOrCreate(String)} to obtain an instance of
   * the attribute holder wrapper for a slot identifier.
   *
   * @param attribute The attribute the modifier is for.
   * @param modifier  The new attribute modifier.
   * @param slot      The slot identifiers for which the modifier should apply.
   * @return True if the modifier was added, false if it was already present.
   * @apiNote Modifiers must have a unique and consistent {@link ResourceLocation} id, or the
   *     modifier will not be removed when the item is unequipped.
   */
  public boolean addModifier(Holder<Attribute> attribute, AttributeModifier modifier,
                             String... slot) {
    return this.addModifier(attribute, modifier, SlotTypePredicate.builder().withId(slot).build());
  }

  /**
   * Adds a new attribute modifier to the given stack. Two modifiers with the same id may not exist
   * or the same attribute, and this method will fail if one exists.
   *
   * <p>Slot modifiers can be added by calling
   * {@link top.theillusivec4.curios.api.SlotAttribute#getOrCreate(String)} to obtain an instance of
   * the attribute holder wrapper for a slot identifier.
   *
   * @param attribute The attribute the modifier is for.
   * @param modifier  The new attribute modifier.
   * @param slotType  The slot types for which the modifier should apply.
   * @return True if the modifier was added, false if it was already present.
   * @apiNote Modifiers must have a unique and consistent {@link ResourceLocation} id, or the
   *     modifier will not be removed when the item is unequipped.
   */
  public boolean addModifier(Holder<Attribute> attribute, AttributeModifier modifier,
                             ISlotType... slotType) {
    return this.addModifier(attribute, modifier,
                            Arrays.stream(slotType).map(ISlotType::getId).toArray(String[]::new));
  }

  /**
   * Adds a new attribute modifier to the given stack. Two modifiers with the same id may not exist
   * or the same attribute, and this method will fail if one exists.
   *
   * <p>Since this does not specify a slot type to match, this modifier will apply to all slot types
   * found.
   *
   * <p>Slot modifiers can be added by calling
   * {@link top.theillusivec4.curios.api.SlotAttribute#getOrCreate(String)} to obtain an instance of
   * the attribute holder wrapper for a slot identifier.
   *
   * @param attribute The attribute the modifier is for.
   * @param modifier  The new attribute modifier.
   * @return True if the modifier was added, false if it was already present.
   * @apiNote Modifiers must have a unique and consistent {@link ResourceLocation} id, or the
   *     modifier will not be removed when the item is unequipped.
   */
  public boolean addModifier(Holder<Attribute> attribute, AttributeModifier modifier) {
    return this.addModifier(attribute, modifier, SlotTypePredicate.ANY);
  }

  /**
   * Removes an attribute modifier for the target attribute by id.
   *
   * <p>Slot modifiers can be removed by calling
   * {@link top.theillusivec4.curios.api.SlotAttribute#getOrCreate(String)} to obtain an instance of
   * the attribute holder wrapper for a slot identifier.
   *
   * @return True if an attribute modifier was removed, false otherwise.
   */
  public boolean removeModifier(Holder<Attribute> attribute, ResourceLocation modifierId) {
    return this.getBuilder().removeModifier(attribute, modifierId);
  }

  /**
   * Adds a new attribute modifier to the given stack, optionally replacing any existing modifiers
   * with the same id.
   *
   * <p>Slot modifiers can be replaced by calling
   * {@link top.theillusivec4.curios.api.SlotAttribute#getOrCreate(String)} to obtain an instance of
   * the attribute holder wrapper for a slot identifier.
   *
   * @param attribute The attribute the modifier is for.
   * @param modifier  The new attribute modifier.
   * @param slot      The slot identifiers for which the modifier should apply.
   * @apiNote Modifiers must have a unique and consistent {@link ResourceLocation} id, or the
   *     modifier will not be removed when the item is unequipped.
   */
  public void replaceModifier(Holder<Attribute> attribute, AttributeModifier modifier,
                              String... slot) {
    this.replaceModifier(attribute, modifier, SlotTypePredicate.builder().withId(slot).build());
  }

  /**
   * Adds a new attribute modifier to the given stack, optionally replacing any existing modifiers
   * with the same id.
   *
   * <p>Slot modifiers can be replaced by calling
   * {@link top.theillusivec4.curios.api.SlotAttribute#getOrCreate(String)} to obtain an instance of
   * the attribute holder wrapper for a slot identifier.
   *
   * @param attribute The attribute the modifier is for.
   * @param modifier  The new attribute modifier.
   * @param slotType  The slot types for which the modifier should apply.
   * @apiNote Modifiers must have a unique and consistent {@link ResourceLocation} id, or the
   *     modifier will not be removed when the item is unequipped.
   */
  public void replaceModifier(Holder<Attribute> attribute, AttributeModifier modifier,
                              ISlotType... slotType) {
    this.replaceModifier(attribute, modifier,
                         Arrays.stream(slotType).map(ISlotType::getId).toArray(String[]::new));
  }

  /**
   * Adds a new attribute modifier to the given stack, optionally replacing any existing modifiers
   * with the same id.
   *
   * <p>Slot modifiers can be removed by calling
   * {@link top.theillusivec4.curios.api.SlotAttribute#getOrCreate(String)} to obtain an instance of
   * the attribute holder wrapper for a slot identifier.
   *
   * @param attribute         The attribute the modifier is for.
   * @param modifier          The new attribute modifier.
   * @param slotTypePredicate The slot type predicate for which the modifier should apply.
   * @apiNote Modifiers must have a unique and consistent {@link ResourceLocation} id, or the
   *     modifier will not be removed when the item is unequipped.
   */
  public void replaceModifier(Holder<Attribute> attribute, AttributeModifier modifier,
                              SlotTypePredicate slotTypePredicate) {
    this.getBuilder().replaceModifier(attribute, modifier, slotTypePredicate);
  }

  /**
   * Removes modifiers based on a condition.
   *
   * @return True if any modifiers were removed, false otherwise.
   */
  public boolean removeIf(Predicate<CurioAttributeModifiers.Entry> condition) {
    return this.getBuilder().removeIf(condition);
  }

  /**
   * Removes all modifiers for the given attribute.
   *
   * @return True if any modifiers were removed, false otherwise.
   */
  public boolean removeAllModifiersFor(Holder<Attribute> attribute) {
    return this.getBuilder().removeIf(entry -> entry.attributeHolder().equals(attribute));
  }

  /**
   * Removes all modifiers for all attributes.
   */
  public void clearModifiers() {
    this.getBuilder().clear();
  }

  /**
   * Builds a new {@link CurioAttributeModifiers} with the results of this event, returning the
   * {@linkplain #getDefaultModifiers() default modifiers} if no changes were made.
   */
  public CurioAttributeModifiers build() {
    return this.builder == null ? this.defaultModifiers :
           this.builder.build(this.defaultModifiers.showInTooltip());
  }

  /**
   * Returns the builder used for adjusting the attribute modifiers, creating it if it does not
   * already exist.
   *
   * @return The current, or newly created, builder instance.
   */
  private CurioAttributeModifiersBuilder getBuilder() {

    if (this.builder == null) {
      this.builder = new CurioAttributeModifiersBuilder(this.defaultModifiers);
    }
    return this.builder;
  }

  /**
   * Returns an unmodifiable view of the attribute multimap. Use other methods from this event to
   * modify the attributes map.
   *
   * <p>Note that adding attributes based on existing attributes may lead to inconsistent results
   * between the tooltip (client) and the actual attributes (server) if the listener order is
   * different. Using {@link #getOriginalModifiers()} instead will give more consistent results.
   *
   * @deprecated Use {@link #getImmutableModifiers()} to work with {@link CurioAttributeModifiers}
   *     instances instead of the deprecated attribute map workflows.
   */
  @Deprecated(forRemoval = true)
  public Multimap<Holder<Attribute>, AttributeModifier> getModifiers() {
    Multimap<Holder<Attribute>, AttributeModifier> modifierMap = LinkedHashMultimap.create();

    for (CurioAttributeModifiers.Entry modifier : this.build().modifiers()) {
      modifierMap.put(modifier.attributeHolder(), modifier.modifier());
    }
    return Multimaps.unmodifiableMultimap(modifierMap);
  }

  /**
   * Returns the attribute map before any changes from other event listeners was made.
   *
   * @deprecated Use {@link #getDefaultModifiers()} to work with {@link CurioAttributeModifiers}
   *     instances instead of the deprecated attribute map workflows.
   */
  @Deprecated(forRemoval = true)
  public Multimap<Holder<Attribute>, AttributeModifier> getOriginalModifiers() {
    return this.originalMap;
  }

  /**
   * Removes a single modifier for the given attribute.
   *
   * @param attribute Attribute.
   * @param modifier  Modifier instance.
   * @return True if an attribute was removed, false if no change.
   * @deprecated Use {@link #removeModifier(Holder, ResourceLocation)} instead to avoid needing
   *     a specific modifier instance.
   */
  @Deprecated(forRemoval = true)
  public boolean removeModifier(Holder<Attribute> attribute, AttributeModifier modifier) {
    return this.removeModifier(attribute, modifier.id());
  }

  /**
   * Removes all modifiers for the given attribute.
   *
   * @param attribute Attribute.
   * @return Collection of removed modifiers.
   * @deprecated Use {@link #removeAllModifiersFor(Holder)} since the returned collection is
   *     no longer valid for the new workflow.
   */
  @Deprecated(forRemoval = true)
  public Collection<AttributeModifier> removeAttribute(Holder<Attribute> attribute) {
    List<AttributeModifier> list = new ArrayList<>();

    for (CurioAttributeModifiers.Entry entry : this.getBuilder().entries) {

      if (entry.attributeHolder().equals(attribute)) {
        list.add(entry.modifier());
      }
    }
    this.removeAllModifiersFor(attribute);
    return list;
  }

  /**
   * Advanced version of {@link ItemAttributeModifiers.Builder} which supports removal and better
   * sanity-checking.
   *
   * <p>The original builder only supports additions and does not guarantee that no duplicate
   * modifiers exist for a given id.
   */
  private static class CurioAttributeModifiersBuilder {

    private final List<CurioAttributeModifiers.Entry> entries;
    private final Map<CurioAttributeModifiersBuilder.Key, CurioAttributeModifiers.Entry>
        entriesByKey;

    CurioAttributeModifiersBuilder(CurioAttributeModifiers defaultModifiers) {
      this.entries = new LinkedList<>();
      this.entriesByKey = new HashMap<>(defaultModifiers.modifiers().size());

      for (CurioAttributeModifiers.Entry entry : defaultModifiers.modifiers()) {
        this.entries.add(entry);
        this.entriesByKey.put(
            new CurioAttributeModifiersBuilder.Key(entry.attributeHolder(), entry.modifier().id()),
            entry);
      }
    }

    /**
     * {@return an unmodifiable view of the underlying entry list}
     */
    List<CurioAttributeModifiers.Entry> getEntryView() {
      return Collections.unmodifiableList(this.entries);
    }

    /**
     * Attempts to add a new modifier, refusing if one is already present with the same id.
     *
     * @return True if the modifier was added, false otherwise.
     */
    boolean addModifier(Holder<Attribute> attribute, AttributeModifier modifier,
                        SlotTypePredicate slotTypePredicate) {
      CurioAttributeModifiersBuilder.Key key =
          new CurioAttributeModifiersBuilder.Key(attribute, modifier.id());

      if (this.entriesByKey.containsKey(key)) {
        return false;
      }
      CurioAttributeModifiers.Entry entry =
          new CurioAttributeModifiers.Entry(attribute, modifier, slotTypePredicate);
      this.entries.add(entry);
      this.entriesByKey.put(key, entry);
      return true;
    }

    /**
     * Removes a modifier for the target attribute with the given id.
     *
     * @return True if a modifier was removed, false otherwise.
     */
    boolean removeModifier(Holder<Attribute> attribute, ResourceLocation id) {
      CurioAttributeModifiers.Entry entry =
          this.entriesByKey.remove(new CurioAttributeModifiersBuilder.Key(attribute, id));

      if (entry != null) {
        this.entries.remove(entry);
        return true;
      }
      return false;
    }

    /**
     * Adds a modifier to the list, replacing any existing modifiers with the same id.
     *
     * @return The previous modifier, or null if there was no previous modifier with the same id.
     */
    @Nullable
    CurioAttributeModifiers.Entry replaceModifier(Holder<Attribute> attribute,
                                                  AttributeModifier modifier,
                                                  SlotTypePredicate slotTypePredicate) {
      CurioAttributeModifiersBuilder.Key
          key = new CurioAttributeModifiersBuilder.Key(attribute, modifier.id());
      CurioAttributeModifiers.Entry entry =
          new CurioAttributeModifiers.Entry(attribute, modifier, slotTypePredicate);

      if (this.entriesByKey.containsKey(key)) {
        CurioAttributeModifiers.Entry previousEntry = this.entriesByKey.get(key);
        int index = this.entries.indexOf(previousEntry);

        if (index != -1) {
          this.entries.set(index, entry);
        } else { // This should never happen, but it can't hurt to have anyway
          this.entries.add(entry);
        }
        this.entriesByKey.put(key, entry);
        return previousEntry;
      } else {
        this.entries.add(entry);
        this.entriesByKey.put(key, entry);
        return null;
      }
    }

    /**
     * Removes modifiers based on a condition.
     *
     * @return true if any modifiers were removed
     */
    boolean removeIf(Predicate<CurioAttributeModifiers.Entry> condition) {
      this.entries.removeIf(condition);
      return this.entriesByKey.values().removeIf(condition);
    }

    void clear() {
      this.entries.clear();
      this.entriesByKey.clear();
    }

    public CurioAttributeModifiers build(boolean showInTooltip) {
      return new CurioAttributeModifiers(ImmutableList.copyOf(this.entries), showInTooltip);
    }

    /**
     * Internal key class. Attribute modifiers are unique by id for each Attribute.
     */
    private record Key(Holder<? extends Attribute> attribute, ResourceLocation id) {

    }
  }
}
