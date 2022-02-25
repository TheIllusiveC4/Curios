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

package top.theillusivec4.curios.common.inventory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.CuriosHelper;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

public class CurioStacksHandler implements ICurioStacksHandler {

  private static final UUID LEGACY_UUID = UUID.fromString("0b0eabbd-4220-4e9f-bafb-34100da2bd7e");

  private final ICuriosItemHandler itemHandler;
  private final String identifier;
  private final Map<UUID, AttributeModifier> modifiers = new HashMap<>();
  private final Set<AttributeModifier> persistentModifiers = new HashSet<>();
  private final Set<AttributeModifier> cachedModifiers = new HashSet<>();
  private final Multimap<AttributeModifier.Operation, AttributeModifier> modifiersByOperation =
      HashMultimap.create();

  private int baseSize;
  private IDynamicStackHandler stackHandler;
  private IDynamicStackHandler cosmeticStackHandler;
  private boolean visible;
  private boolean cosmetic;
  private NonNullList<Boolean> renderHandler;
  private boolean update;

  public CurioStacksHandler(ICuriosItemHandler itemHandler, String identifier) {
    this(itemHandler, identifier, 1, 0, true, false);
  }

  public CurioStacksHandler(ICuriosItemHandler itemHandler, String identifier, int size, int shift,
                            boolean visible, boolean cosmetic) {
    this.baseSize = size;
    this.visible = visible;
    this.cosmetic = cosmetic;
    this.itemHandler = itemHandler;
    this.identifier = identifier;
    this.stackHandler = new DynamicStackHandler(size);
    this.cosmeticStackHandler = new DynamicStackHandler(size);
    this.renderHandler = NonNullList.withSize(size, true);
  }

  @Override
  public IDynamicStackHandler getStacks() {
    this.update();
    return this.stackHandler;
  }

  @Override
  public IDynamicStackHandler getCosmeticStacks() {
    this.update();
    return this.cosmeticStackHandler;
  }

  @Override
  public NonNullList<Boolean> getRenders() {
    this.update();
    return this.renderHandler;
  }

  @Override
  public int getSlots() {
    this.update();
    return this.stackHandler.getSlots();
  }

  @Override
  public int getSizeShift() {
    return 0;
  }

  @Override
  public boolean isVisible() {
    return this.visible;
  }

  @Override
  public boolean hasCosmetic() {
    return this.cosmetic;
  }

  @Override
  public void grow(int amount) {
    amount = Math.max(0, amount);

    if (amount > 0) {
      this.addLegacyChange(amount);
    }
  }

  @Override
  public void shrink(int amount) {
    amount = Math.max(0, amount);

    if (amount > 0) {
      this.addLegacyChange(Math.min(this.getSlots(), amount) * -1);
    }
  }

  private void addLegacyChange(int shift) {
    AttributeModifier mod = this.getModifiers().get(LEGACY_UUID);
    int current = mod != null ? (int) mod.getAmount() : 0;
    current += shift;
    AttributeModifier newModifier =
        new AttributeModifier(LEGACY_UUID, "legacy", current, AttributeModifier.Operation.ADDITION);
    this.modifiers.put(newModifier.getID(), newModifier);
    Collection<AttributeModifier> modifiers =
        this.getModifiersByOperation(newModifier.getOperation());
    modifiers.remove(newModifier);
    modifiers.add(newModifier);
    this.persistentModifiers.remove(newModifier);
    this.persistentModifiers.add(newModifier);
    this.flagUpdate();
  }

  @Override
  public CompoundNBT serializeNBT() {
    CompoundNBT compoundNBT = new CompoundNBT();
    compoundNBT.putInt("SavedBaseSize", this.baseSize);
    compoundNBT.put("Stacks", this.stackHandler.serializeNBT());
    compoundNBT.put("Cosmetics", this.cosmeticStackHandler.serializeNBT());

    ListNBT nbtTagList = new ListNBT();

    for (int i = 0; i < this.renderHandler.size(); i++) {
      CompoundNBT tag = new CompoundNBT();
      tag.putInt("Slot", i);
      tag.putBoolean("Render", this.renderHandler.get(i));
      nbtTagList.add(tag);
    }
    CompoundNBT nbt = new CompoundNBT();
    nbt.put("Renders", nbtTagList);
    nbt.putInt("Size", this.renderHandler.size());
    compoundNBT.put("Renders", nbt);
    compoundNBT.putBoolean("HasCosmetic", this.cosmetic);
    compoundNBT.putBoolean("Visible", this.visible);

    if (!this.persistentModifiers.isEmpty()) {
      ListNBT list = new ListNBT();

      for (AttributeModifier attributeModifier : this.persistentModifiers) {
        list.add(attributeModifier.write());
      }
      compoundNBT.put("PersistentModifiers", list);
    }

    if (!this.modifiers.isEmpty()) {
      ListNBT list = new ListNBT();
      this.modifiers.forEach((uuid, modifier) -> {
        if (!this.persistentModifiers.contains(modifier)) {
          list.add(modifier.write());
        }
      });
      compoundNBT.put("CachedModifiers", list);
    }
    return compoundNBT;
  }

  @Override
  public void deserializeNBT(CompoundNBT nbt) {

    if (nbt.contains("SavedBaseSize")) {
      this.baseSize = nbt.getInt("SavedBaseSize");
    }

    if (nbt.contains("Stacks")) {
      this.stackHandler.deserializeNBT(nbt.getCompound("Stacks"));
    }

    if (nbt.contains("Cosmetics")) {
      this.cosmeticStackHandler.deserializeNBT(nbt.getCompound("Cosmetics"));
    }

    if (nbt.contains("Renders")) {
      CompoundNBT tag = nbt.getCompound("Renders");
      this.renderHandler = NonNullList.withSize(
          nbt.contains("Size", Constants.NBT.TAG_INT) ? nbt.getInt("Size")
              : this.stackHandler.getSlots(), true);
      ListNBT tagList = tag.getList("Renders", Constants.NBT.TAG_COMPOUND);

      for (int i = 0; i < tagList.size(); i++) {
        CompoundNBT tags = tagList.getCompound(i);
        int slot = tags.getInt("Slot");

        if (slot >= 0 && slot < this.renderHandler.size()) {
          this.renderHandler.set(slot, tags.getBoolean("Render"));
        }
      }
    }

    if (nbt.contains("SizeShift")) {
      int sizeShift = nbt.getInt("SizeShift");

      if (sizeShift != 0) {
        this.addLegacyChange(sizeShift);
      }
    }
    this.cosmetic = nbt.contains("HasCosmetic") ? nbt.getBoolean("HasCosmetic") : this.cosmetic;
    this.visible = nbt.contains("Visible") ? nbt.getBoolean("Visible") : this.visible;

    if (nbt.contains("PersistentModifiers", 9)) {
      ListNBT list = nbt.getList("PersistentModifiers", 10);

      for (int i = 0; i < list.size(); ++i) {
        AttributeModifier attributeModifier = AttributeModifier.read(list.getCompound(i));

        if (attributeModifier != null) {
          this.addPermanentModifier(attributeModifier);
        }
      }
    }

    if (nbt.contains("CachedModifiers", 9)) {
      ListNBT list = nbt.getList("CachedModifiers", 10);

      for (int i = 0; i < list.size(); ++i) {
        AttributeModifier attributeModifier = AttributeModifier.read(list.getCompound(i));

        if (attributeModifier != null) {
          this.cachedModifiers.add(attributeModifier);
          this.addTransientModifier(attributeModifier);
        }
      }
    }
    this.update();
  }

  @Override
  public String getIdentifier() {
    return this.identifier;
  }

  public CompoundNBT getSyncTag() {
    CompoundNBT compoundNBT = new CompoundNBT();
    compoundNBT.put("Stacks", this.stackHandler.serializeNBT());
    compoundNBT.put("Cosmetics", this.cosmeticStackHandler.serializeNBT());

    ListNBT nbtTagList = new ListNBT();

    for (int i = 0; i < this.renderHandler.size(); i++) {
      CompoundNBT tag = new CompoundNBT();
      tag.putInt("Slot", i);
      tag.putBoolean("Render", this.renderHandler.get(i));
      nbtTagList.add(tag);
    }
    CompoundNBT nbt = new CompoundNBT();
    nbt.put("Renders", nbtTagList);
    nbt.putInt("Size", this.renderHandler.size());
    compoundNBT.put("Renders", nbt);
    compoundNBT.putBoolean("HasCosmetic", this.cosmetic);
    compoundNBT.putBoolean("Visible", this.visible);
    compoundNBT.putInt("BaseSize", this.baseSize);

    if (!this.modifiers.isEmpty()) {
      ListNBT list = new ListNBT();

      for (Map.Entry<UUID, AttributeModifier> modifier : this.modifiers.entrySet()) {
        list.add(modifier.getValue().write());
      }
      compoundNBT.put("Modifiers", list);
    }
    return compoundNBT;
  }

  public void applySyncTag(CompoundNBT tag) {

    if (tag.contains("BaseSize")) {
      this.baseSize = tag.getInt("BaseSize");
    }

    if (tag.contains("Stacks")) {
      this.stackHandler.deserializeNBT(tag.getCompound("Stacks"));
    }

    if (tag.contains("Cosmetics")) {
      this.cosmeticStackHandler.deserializeNBT(tag.getCompound("Cosmetics"));
    }

    if (tag.contains("Renders")) {
      CompoundNBT compoundNBT = tag.getCompound("Renders");
      this.renderHandler = NonNullList.withSize(
          compoundNBT.contains("Size", Constants.NBT.TAG_INT) ? compoundNBT.getInt("Size")
              : this.stackHandler.getSlots(), true);
      ListNBT tagList = compoundNBT.getList("Renders", Constants.NBT.TAG_COMPOUND);

      for (int i = 0; i < tagList.size(); i++) {
        CompoundNBT tags = tagList.getCompound(i);
        int slot = tags.getInt("Slot");

        if (slot >= 0 && slot < this.renderHandler.size()) {
          this.renderHandler.set(slot, tags.getBoolean("Render"));
        }
      }
    }

    if (tag.contains("SizeShift")) {
      int sizeShift = tag.getInt("SizeShift");

      if (sizeShift != 0) {
        this.addLegacyChange(sizeShift);
      }
    }
    this.cosmetic = tag.contains("HasCosmetic") ? tag.getBoolean("HasCosmetic") : this.cosmetic;
    this.visible = tag.contains("Visible") ? tag.getBoolean("Visible") : this.visible;
    this.modifiers.clear();
    this.persistentModifiers.clear();
    this.modifiersByOperation.clear();

    if (tag.contains("Modifiers", 9)) {
      ListNBT list = tag.getList("Modifiers", 10);

      for (int i = 0; i < list.size(); ++i) {
        AttributeModifier attributeModifier = AttributeModifier.read(list.getCompound(i));

        if (attributeModifier != null) {
          this.addTransientModifier(attributeModifier);
        }
      }
    }
    this.flagUpdate();
    this.update();
  }

  @Override
  public void copyModifiers(ICurioStacksHandler other) {
    this.modifiers.clear();
    this.cachedModifiers.clear();
    this.modifiersByOperation.clear();
    this.persistentModifiers.clear();
    other.getModifiers().forEach((uuid, modifier) -> this.addTransientModifier(modifier));
    this.cachedModifiers.addAll(other.getCachedModifiers());

    for (AttributeModifier persistentModifier : other.getPermanentModifiers()) {
      this.addPermanentModifier(persistentModifier);
    }
    this.update();
  }

  public Map<UUID, AttributeModifier> getModifiers() {
    return this.modifiers;
  }

  @Override
  public Set<AttributeModifier> getPermanentModifiers() {
    return this.persistentModifiers;
  }

  @Override
  public Set<AttributeModifier> getCachedModifiers() {
    return this.cachedModifiers;
  }

  public Collection<AttributeModifier> getModifiersByOperation(
      AttributeModifier.Operation operation) {
    return this.modifiersByOperation.get(operation);
  }

  public void addTransientModifier(AttributeModifier modifier) {
    this.modifiers.put(modifier.getID(), modifier);
    this.getModifiersByOperation(modifier.getOperation()).add(modifier);
    this.flagUpdate();
  }

  public void addPermanentModifier(AttributeModifier modifier) {
    this.addTransientModifier(modifier);
    this.persistentModifiers.add(modifier);
  }

  public void removeModifier(UUID uuid) {
    AttributeModifier modifier = this.modifiers.remove(uuid);

    if (modifier != null) {
      this.persistentModifiers.remove(modifier);
      this.getModifiersByOperation(modifier.getOperation()).remove(modifier);
      this.flagUpdate();
    }
  }

  private void flagUpdate() {
    this.update = true;

    if (this.itemHandler != null) {
      this.itemHandler.getUpdatingInventories().remove(this);
      this.itemHandler.getUpdatingInventories().add(this);
    }
  }

  public void clearModifiers() {
    Set<UUID> ids = new HashSet<>(this.modifiers.keySet());

    for (UUID id : ids) {
      this.removeModifier(id);
    }
  }

  public void clearCachedModifiers() {

    for (AttributeModifier cachedModifier : this.cachedModifiers) {
      this.removeModifier(cachedModifier.getID());
    }
    this.cachedModifiers.clear();
    this.flagUpdate();
  }

  public void update() {

    if (this.update) {
      this.update = false;
      double baseSize = this.baseSize;

      for (AttributeModifier mod : this.getModifiersByOperation(
          AttributeModifier.Operation.ADDITION)) {
        baseSize += mod.getAmount();
      }
      double size = baseSize;

      for (AttributeModifier mod : this.getModifiersByOperation(
          AttributeModifier.Operation.MULTIPLY_BASE)) {
        size += this.baseSize * mod.getAmount();
      }

      for (AttributeModifier mod : this.getModifiersByOperation(
          AttributeModifier.Operation.MULTIPLY_TOTAL)) {
        size *= mod.getAmount();
      }

      if (size != this.getSlots()) {
        this.resize((int) size);

        if (this.itemHandler != null && this.itemHandler.getWearer() instanceof PlayerEntity) {
          PlayerEntity player = (PlayerEntity) this.itemHandler.getWearer();

          if (player.openContainer instanceof CuriosContainer) {
            ((CuriosContainer) player.openContainer).resetSlots();
          }
        }
      }
    }
  }

  private void resize(int newSize) {
    int currentSize = this.getSlots();

    if (currentSize != newSize) {
      int change = newSize - currentSize;

      if (currentSize > newSize) {
        change = change * -1;
        this.loseStacks(this.stackHandler, identifier, change);
        this.stackHandler.shrink(change);
        this.cosmeticStackHandler.shrink(change);
        NonNullList<Boolean> newList = NonNullList.withSize(newSize, true);

        for (int i = 0; i < newList.size() && i < this.renderHandler.size(); i++) {
          newList.set(i, renderHandler.get(i));
        }
        this.renderHandler = newList;
      } else {
        this.stackHandler.grow(change);
        this.cosmeticStackHandler.grow(change);
        NonNullList<Boolean> newList = NonNullList.withSize(newSize, true);

        for (int i = 0; i < newList.size() && i < this.renderHandler.size(); i++) {
          newList.set(i, renderHandler.get(i));
        }
        this.renderHandler = newList;
      }
    }
  }

  private void loseStacks(IDynamicStackHandler stackHandler, String identifier, int amount) {

    if (this.itemHandler == null) {
      return;
    }
    List<ItemStack> drops = new ArrayList<>();

    for (int i = Math.max(0, stackHandler.getSlots() - amount);
         i >= 0 && i < stackHandler.getSlots(); i++) {
      ItemStack stack = stackHandler.getStackInSlot(i);
      drops.add(stackHandler.getStackInSlot(i));
      LivingEntity entity = this.itemHandler.getWearer();
      SlotContext slotContext = new SlotContext(identifier, entity, i);

      if (!stack.isEmpty()) {
        UUID uuid = UUID.nameUUIDFromBytes((identifier + i).getBytes());
        Multimap<Attribute, AttributeModifier> map =
            CuriosApi.getCuriosHelper().getAttributeModifiers(slotContext, uuid, stack);
        Multimap<String, AttributeModifier> slots = HashMultimap.create();
        Set<CuriosHelper.SlotAttributeWrapper> toRemove = new HashSet<>();

        for (Attribute attribute : map.keySet()) {

          if (attribute instanceof CuriosHelper.SlotAttributeWrapper) {
            CuriosHelper.SlotAttributeWrapper wrapper =
                (CuriosHelper.SlotAttributeWrapper) attribute;
            slots.putAll(wrapper.identifier, map.get(attribute));
            toRemove.add(wrapper);
          }
        }

        for (Attribute attribute : toRemove) {
          map.removeAll(attribute);
        }
        this.itemHandler.getWearer().getAttributeManager().removeModifiers(map);
        this.itemHandler.removeSlotModifiers(slots);
        CuriosApi.getCuriosHelper().getCurio(stack)
            .ifPresent(curio -> curio.onUnequip(slotContext, ItemStack.EMPTY));
      }
      stackHandler.setStackInSlot(i, ItemStack.EMPTY);
    }
    drops.forEach(this.itemHandler::loseInvalidStack);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CurioStacksHandler that = (CurioStacksHandler) o;
    return identifier.equals(that.identifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifier);
  }
}
