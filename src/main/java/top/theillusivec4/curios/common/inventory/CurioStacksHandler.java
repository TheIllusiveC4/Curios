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
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.EnumUtils;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosResources;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.event.SlotModifiersUpdatedEvent;
import top.theillusivec4.curios.api.type.ICuriosMenu;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncActiveState;
import top.theillusivec4.curios.impl.CuriosRegistry;

public class CurioStacksHandler implements ICurioStacksHandler {

  private static final ResourceLocation LEGACY_ID = CuriosResources.resource("legacy");

  private final ICuriosItemHandler itemHandler;
  private final String identifier;
  private final Map<ResourceLocation, AttributeModifier> modifiers = new HashMap<>();
  private final Map<ResourceLocation, AttributeModifier> persistentModifiers = new HashMap<>();
  private final Map<ResourceLocation, AttributeModifier> cachedModifiers = new HashMap<>();
  private final Multimap<AttributeModifier.Operation, AttributeModifier> modifiersByOperation =
      HashMultimap.create();

  private int baseSize;
  private final IDynamicStackHandler stackHandler;
  private final IDynamicStackHandler cosmeticStackHandler;
  private boolean visible;
  private boolean cosmetic;
  private boolean canToggleRender;
  private DropRule dropRule;
  private boolean update;
  private NonNullList<Boolean> renderHandler;
  private NonNullList<Boolean> activeStates;
  private NonNullList<Boolean> previousActiveStates;

  private int clearCacheTick = -1;

  public CurioStacksHandler(ICuriosItemHandler itemHandler, String identifier) {
    this(itemHandler, identifier, 1, true, false, true, DropRule.DEFAULT);
  }

  public CurioStacksHandler(
      ICuriosItemHandler itemHandler,
      String identifier,
      int size,
      boolean visible,
      boolean cosmetic,
      boolean canToggleRender,
      DropRule dropRule) {
    this.baseSize = size;
    this.visible = visible;
    this.cosmetic = cosmetic;
    this.itemHandler = itemHandler;
    this.identifier = identifier;
    this.canToggleRender = canToggleRender;
    this.dropRule = dropRule;
    this.renderHandler = NonNullList.withSize(size, true);
    this.activeStates = NonNullList.withSize(size, true);
    this.previousActiveStates = NonNullList.withSize(size, true);
    this.stackHandler =
        new DynamicStackHandler(
            size,
            (index) ->
                new SlotContext(
                    identifier,
                    itemHandler.getWearer(),
                    index,
                    false,
                    this.getRenders().get(index)));
    this.cosmeticStackHandler =
        new DynamicStackHandler(
            size,
            (index) ->
                new SlotContext(
                    identifier,
                    itemHandler.getWearer(),
                    index,
                    true,
                    this.getRenders().get(index)));
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
  public NonNullList<Boolean> getActiveStates() {
    this.update();
    return this.activeStates;
  }

  @Override
  public void updateActiveState(int index) {
    this.update();
    LivingEntity livingEntity = this.itemHandler.getWearer();

    if (livingEntity != null && !livingEntity.level().isClientSide()) {

      if (this.activeStates.size() <= index) {
        return;
      }
      boolean current = this.activeStates.get(index);
      boolean previous = this.previousActiveStates.get(index);

      if (current == previous) {
        return;
      }

      if (!previous) {
        activateSlot(index);
      } else {
        deactivateSlot(index);
      }
    }
  }

  private void deactivateSlot(int index) {
    this.previousActiveStates.set(index, false);
    LivingEntity livingEntity = this.itemHandler.getWearer();
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(livingEntity, new SPacketSyncActiveState(
        livingEntity.getId(), identifier, index, false));
    NonNullList<Boolean> renderStates = this.getRenders();
    SlotContext slotContext =
        new SlotContext(
            identifier,
            livingEntity,
            index,
            false,
            renderStates.size() > index && renderStates.get(index));
    IDynamicStackHandler stacks = this.getStacks();
    ItemStack stack = stacks.getStackInSlot(index);
    NeoForge.EVENT_BUS.post(
        new CurioChangeEvent.Item(livingEntity, identifier, index, stack, ItemStack.EMPTY));
    AttributeMap attributeMap = livingEntity.getAttributes();

    if (!stack.isEmpty()) {
      ICurioItem
          .forEachModifier(stack, slotContext,
                           (attributeHolder, attributeModifier) -> {
                             if (attributeHolder.value() instanceof SlotAttribute slotAttribute) {
                               this.itemHandler.removeSlotModifier(
                                   slotAttribute.id(),
                                   attributeModifier.id());
                             } else {
                               AttributeInstance instance =
                                   attributeMap.getInstance(attributeHolder);

                               if (instance != null) {
                                 instance.removeModifier(attributeModifier);
                               }
                             }
                           });
      CuriosApi.getCurio(stack).ifPresent(curio -> curio.onUnequip(slotContext, stack));
    }
  }

  private void activateSlot(int index) {
    this.previousActiveStates.set(index, true);
    LivingEntity livingEntity = this.itemHandler.getWearer();
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(
        livingEntity,
        new SPacketSyncActiveState(livingEntity.getId(), identifier, index, true));
    NonNullList<Boolean> renderStates = this.getRenders();
    SlotContext slotContext =
        new SlotContext(
            identifier,
            livingEntity,
            index,
            false,
            renderStates.size() > index && renderStates.get(index));
    IDynamicStackHandler stacks = this.getStacks();
    ItemStack stack = stacks.getStackInSlot(index);
    NeoForge.EVENT_BUS.post(
        new CurioChangeEvent.Item(livingEntity, identifier, index, ItemStack.EMPTY, stack));
    AttributeMap attributeMap = livingEntity.getAttributes();

    if (!stack.isEmpty()) {
      ICurioItem
          .forEachModifier(stack, slotContext,
                           (attributeHolder, attributeModifier) -> {
                             if (attributeHolder.value() instanceof SlotAttribute slotAttribute) {
                               this.itemHandler.addTransientSlotModifier(
                                   slotAttribute.id(),
                                   attributeModifier.id(), attributeModifier.amount(),
                                   attributeModifier.operation());
                             } else {
                               AttributeInstance instance =
                                   attributeMap.getInstance(attributeHolder);

                               if (instance != null) {
                                 instance.addOrUpdateTransientModifier(
                                     attributeModifier);
                               }
                             }
                           });
      CuriosApi.getCurio(stack).ifPresent(curio -> curio.onEquip(slotContext, ItemStack.EMPTY));

      if (livingEntity instanceof ServerPlayer) {
        CuriosRegistry.EQUIP_TRIGGER.get().trigger(slotContext, (ServerPlayer) livingEntity, stack);
      }
    }
  }

  @Override
  public boolean canToggleRendering() {
    return this.canToggleRender;
  }

  @Override
  public DropRule getDropRule() {
    return this.dropRule;
  }

  @Override
  public int getSlots() {
    this.update();
    return this.stackHandler.getSlots();
  }

  @Override
  public boolean isVisible() {
    return this.visible;
  }

  @Override
  public boolean hasCosmetic() {
    return this.cosmetic;
  }

  private void addLegacyChange(int shift) {
    AttributeModifier mod = this.getModifiers().get(LEGACY_ID);
    int current = mod != null ? (int) mod.amount() : 0;
    current += shift;
    AttributeModifier newModifier =
        new AttributeModifier(LEGACY_ID, current, AttributeModifier.Operation.ADD_VALUE);
    this.modifiers.put(newModifier.id(), newModifier);
    Collection<AttributeModifier> modifiers = this.getModifiersByOperation(newModifier.operation());
    List<AttributeModifier> ops = new ArrayList<>(modifiers);

    for (AttributeModifier op : ops) {

      if (op.id().equals(newModifier.id())) {
        modifiers.remove(op);
      }
    }
    modifiers.add(newModifier);
    this.persistentModifiers.remove(newModifier.id());
    this.persistentModifiers.put(newModifier.id(), newModifier);
    this.flagUpdate();
  }

  @Override
  public CompoundTag serializeNBT() {
    CompoundTag compoundNBT = new CompoundTag();
    compoundNBT.putInt("SavedBaseSize", this.baseSize);
    compoundNBT.put(
        "Stacks", this.stackHandler.serializeNBT(this.itemHandler.getWearer().registryAccess()));
    compoundNBT.put(
        "Cosmetics",
        this.cosmeticStackHandler.serializeNBT(this.itemHandler.getWearer().registryAccess()));

    ListTag nbtTagList = new ListTag();

    for (int i = 0; i < this.renderHandler.size(); i++) {
      CompoundTag tag = new CompoundTag();
      tag.putInt("Slot", i);
      tag.putBoolean("Render", this.renderHandler.get(i));
      nbtTagList.add(tag);
    }
    CompoundTag nbt = new CompoundTag();
    nbt.put("Renders", nbtTagList);
    nbt.putInt("Size", this.renderHandler.size());
    compoundNBT.put("Renders", nbt);
    nbtTagList = new ListTag();

    for (int i = 0; i < this.activeStates.size(); i++) {
      CompoundTag tag = new CompoundTag();
      tag.putInt("Slot", i);
      tag.putBoolean("ActiveState", this.activeStates.get(i));
      nbtTagList.add(tag);
    }
    nbt = new CompoundTag();
    nbt.put("ActiveStates", nbtTagList);
    nbt.putInt("Size", this.activeStates.size());
    compoundNBT.put("ActiveStates", nbt);
    compoundNBT.putBoolean("HasCosmetic", this.cosmetic);
    compoundNBT.putBoolean("Visible", this.visible);
    compoundNBT.putBoolean("RenderToggle", this.canToggleRender);
    compoundNBT.putString("DropRule", this.dropRule.toString());

    if (!this.persistentModifiers.isEmpty()) {
      compoundNBT.store("PersistentModifiers", AttributeModifier.CODEC.listOf(),
                        List.copyOf(this.persistentModifiers.values()));
    }

    if (!this.modifiers.isEmpty()) {
      ListTag list = new ListTag();
      this.modifiers.forEach(
          (uuid, modifier) -> {
            if (!this.persistentModifiers.containsKey(modifier.id())) {
              CompoundTag tag = new CompoundTag();
              tag.store(AttributeModifier.MAP_CODEC, modifier);
              list.add(tag);
            }
          });
      compoundNBT.put("CachedModifiers", list);
    }
    return compoundNBT;
  }

  @Override
  public void deserializeNBT(CompoundTag nbt) {

    if (nbt.contains("SavedBaseSize")) {
      this.baseSize = nbt.getInt("SavedBaseSize").orElse(0);
    }

    if (nbt.contains("Stacks")) {
      this.stackHandler.deserializeNBT(
          this.itemHandler.getWearer().registryAccess(),
          nbt.getCompound("Stacks").orElse(new CompoundTag()));
    }

    if (nbt.contains("Cosmetics")) {
      this.cosmeticStackHandler.deserializeNBT(
          this.itemHandler.getWearer().registryAccess(),
          nbt.getCompound("Cosmetics").orElse(new CompoundTag()));
    }

    if (nbt.contains("Renders")) {
      CompoundTag tag = nbt.getCompound("Renders").orElse(new CompoundTag());
      this.renderHandler =
          NonNullList.withSize(
              nbt.contains("Size") ? nbt.getInt("Size").orElse(0) : this.stackHandler.getSlots(),
              true);
      ListTag tagList = tag.getList("Renders").orElse(new ListTag());

      for (int i = 0; i < tagList.size(); i++) {
        CompoundTag tags = tagList.getCompound(i).orElse(new CompoundTag());
        int slot = tags.getInt("Slot").orElse(0);

        if (slot >= 0 && slot < this.renderHandler.size()) {
          this.renderHandler.set(slot, tags.getBoolean("Render").orElse(true));
        }
      }
    }

    if (nbt.contains("ActiveStates")) {
      CompoundTag tag = nbt.getCompound("ActiveStates").orElse(new CompoundTag());
      this.activeStates = NonNullList.withSize(
          nbt.contains("Size") ? nbt.getInt("Size").orElse(0) : this.stackHandler.getSlots(),
          true);
      this.previousActiveStates = NonNullList.withSize(
          nbt.contains("Size") ? nbt.getInt("Size").orElse(0) : this.stackHandler.getSlots(),
          true);
      ListTag tagList = tag.getList("ActiveStates").orElse(new ListTag());

      for (int i = 0; i < tagList.size(); i++) {
        CompoundTag tags = tagList.getCompound(i).orElse(new CompoundTag());
        int slot = tags.getInt("Slot").orElse(0);

        if (slot >= 0 && slot < this.activeStates.size()) {
          this.activeStates.set(slot, tags.getBoolean("ActiveState").orElse(true));
          this.previousActiveStates.set(slot, tags.getBoolean("ActiveState").orElse(true));
        }
      }
    }

    if (nbt.contains("SizeShift")) {
      int sizeShift = nbt.getInt("SizeShift").orElse(0);

      if (sizeShift != 0) {
        this.addLegacyChange(sizeShift);
      }
    }
    this.cosmetic =
        nbt.contains("HasCosmetic") ? nbt.getBoolean("HasCosmetic").orElse(false) : this.cosmetic;
    this.visible = nbt.contains("Visible") ? nbt.getBoolean("Visible").orElse(true) : this.visible;
    this.canToggleRender =
        nbt.contains("RenderToggle") ? nbt.getBoolean("RenderToggle").orElse(true)
                                     : this.canToggleRender;

    if (nbt.contains("DropRule")) {
      this.dropRule =
          EnumUtils.getEnum(DropRule.class,
                            nbt.getString("DropRule").orElse(DropRule.DEFAULT.getSerializedName()),
                            this.dropRule);
    }

    if (nbt.contains("PersistentModifiers")) {

      for (AttributeModifier modifier : nbt.read("PersistentModifiers",
                                                 AttributeModifier.CODEC.listOf())
          .orElse(List.of())) {
        this.addPermanentModifier(modifier);
      }
    }

    if (nbt.contains("CachedModifiers")) {
      ListTag list = nbt.getList("CachedModifiers").orElse(new ListTag());

      for (int i = 0; i < list.size(); ++i) {
        CompoundTag tag = list.getCompound(i).orElse(new CompoundTag());
        AttributeModifier attributeModifier = tag.read(AttributeModifier.MAP_CODEC).orElse(null);

        if (attributeModifier != null) {
          this.cachedModifiers.put(attributeModifier.id(), attributeModifier);
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

  public CompoundTag getSyncTag() {
    CompoundTag compoundNBT = new CompoundTag();
    compoundNBT.put(
        "Stacks", this.stackHandler.serializeNBT(this.itemHandler.getWearer().registryAccess()));
    compoundNBT.put(
        "Cosmetics",
        this.cosmeticStackHandler.serializeNBT(this.itemHandler.getWearer().registryAccess()));

    ListTag nbtTagList = new ListTag();

    for (int i = 0; i < this.renderHandler.size(); i++) {
      CompoundTag tag = new CompoundTag();
      tag.putInt("Slot", i);
      tag.putBoolean("Render", this.renderHandler.get(i));
      nbtTagList.add(tag);
    }
    CompoundTag nbt = new CompoundTag();
    nbt.put("Renders", nbtTagList);
    nbt.putInt("Size", this.renderHandler.size());
    compoundNBT.put("Renders", nbt);
    nbtTagList = new ListTag();

    for (int i = 0; i < this.activeStates.size(); i++) {
      CompoundTag tag = new CompoundTag();
      tag.putInt("Slot", i);
      tag.putBoolean("ActiveState", this.activeStates.get(i));
      nbtTagList.add(tag);
    }
    nbt = new CompoundTag();
    nbt.put("ActiveStates", nbtTagList);
    nbt.putInt("Size", this.activeStates.size());
    compoundNBT.put("ActiveStates", nbt);
    compoundNBT.putBoolean("HasCosmetic", this.cosmetic);
    compoundNBT.putBoolean("Visible", this.visible);
    compoundNBT.putBoolean("RenderToggle", this.canToggleRender);
    compoundNBT.putString("DropRule", this.dropRule.toString());
    compoundNBT.putInt("BaseSize", this.baseSize);

    if (!this.modifiers.isEmpty()) {
      compoundNBT.store("Modifiers", AttributeModifier.CODEC.listOf(),
                        List.copyOf(this.modifiers.values()));
    }
    return compoundNBT;
  }

  public void applySyncTag(CompoundTag tag) {

    if (tag.contains("BaseSize")) {
      this.baseSize = tag.getInt("BaseSize").orElse(0);
    }

    if (tag.contains("Stacks")) {
      this.stackHandler.deserializeNBT(
          this.itemHandler.getWearer().registryAccess(), tag.getCompound("Stacks")
              .orElse(new CompoundTag()));
    }

    if (tag.contains("Cosmetics")) {
      this.cosmeticStackHandler.deserializeNBT(
          this.itemHandler.getWearer().registryAccess(), tag.getCompound("Cosmetics")
              .orElse(new CompoundTag()));
    }

    if (tag.contains("Renders")) {
      CompoundTag compoundNBT = tag.getCompound("Renders").orElse(new CompoundTag());
      this.renderHandler =
          NonNullList.withSize(
              compoundNBT.contains("Size")
              ? compoundNBT.getInt("Size").orElse(0)
              : this.stackHandler.getSlots(),
              true);
      ListTag tagList = compoundNBT.getList("Renders").orElse(new ListTag());

      for (int i = 0; i < tagList.size(); i++) {
        CompoundTag tags = tagList.getCompound(i).orElse(new CompoundTag());
        int slot = tags.getInt("Slot").orElse(0);

        if (slot >= 0 && slot < this.renderHandler.size()) {
          this.renderHandler.set(slot, tags.getBoolean("Render").orElse(true));
        }
      }
    }

    if (tag.contains("ActiveStates")) {
      CompoundTag compoundNBT = tag.getCompound("ActiveStates").orElse(new CompoundTag());
      this.activeStates = NonNullList.withSize(
          compoundNBT.contains("Size") ? compoundNBT.getInt("Size").orElse(0) :
          this.stackHandler.getSlots(), true);
      this.previousActiveStates = NonNullList.withSize(
          compoundNBT.contains("Size") ? compoundNBT.getInt("Size").orElse(0)
                                       : this.stackHandler.getSlots(),
          true);
      ListTag tagList = compoundNBT.getList("ActiveStates").orElse(new ListTag());

      for (int i = 0; i < tagList.size(); i++) {
        CompoundTag tags = tagList.getCompound(i).orElse(new CompoundTag());
        int slot = tags.getInt("Slot").orElse(0);

        if (slot >= 0 && slot < this.activeStates.size()) {
          this.activeStates.set(slot, tags.getBoolean("ActiveState").orElse(true));
          this.previousActiveStates.set(slot, tags.getBoolean("ActiveState").orElse(true));
        }
      }
    }

    if (tag.contains("SizeShift")) {
      int sizeShift = tag.getInt("SizeShift").orElse(0);

      if (sizeShift != 0) {
        this.addLegacyChange(sizeShift);
      }
    }
    this.cosmetic =
        tag.contains("HasCosmetic") ? tag.getBoolean("HasCosmetic").orElse(false) : this.cosmetic;
    this.visible = tag.contains("Visible") ? tag.getBoolean("Visible").orElse(true) : this.visible;
    this.canToggleRender =
        tag.contains("RenderToggle") ? tag.getBoolean("RenderToggle").orElse(true)
                                     : this.canToggleRender;

    if (tag.contains("DropRule")) {
      this.dropRule =
          EnumUtils.getEnum(DropRule.class,
                            tag.getString("DropRule").orElse(DropRule.DEFAULT.getSerializedName()),
                            this.dropRule);
    }
    this.modifiers.clear();
    this.persistentModifiers.clear();
    this.modifiersByOperation.clear();

    if (tag.contains("Modifiers")) {

      for (AttributeModifier modifier : tag.read("Modifiers",
                                                 AttributeModifier.CODEC.listOf())
          .orElse(List.of())) {
        this.addTransientModifier(modifier);
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

    for (AttributeModifier cachedModifier : other.getCachedModifiers()) {
      this.cachedModifiers.put(cachedModifier.id(), cachedModifier);
    }

    for (AttributeModifier persistentModifier : other.getPermanentModifiers()) {
      this.addPermanentModifier(persistentModifier);
    }
    this.update();
  }

  public Map<ResourceLocation, AttributeModifier> getModifiers() {
    return this.modifiers;
  }

  @Override
  public Set<AttributeModifier> getPermanentModifiers() {
    return new HashSet<>(this.persistentModifiers.values());
  }

  @Override
  public Set<AttributeModifier> getCachedModifiers() {
    return new HashSet<>(this.cachedModifiers.values());
  }

  public Collection<AttributeModifier> getModifiersByOperation(
      AttributeModifier.Operation operation) {
    return this.modifiersByOperation.get(operation);
  }

  public void addTransientModifier(AttributeModifier modifier) {
    this.modifiers.put(modifier.id(), modifier);
    this.getModifiersByOperation(modifier.operation()).add(modifier);
    this.flagUpdate();
  }

  public void addPermanentModifier(AttributeModifier modifier) {
    this.addTransientModifier(modifier);
    this.persistentModifiers.put(modifier.id(), modifier);
  }

  public void removeModifier(ResourceLocation id) {
    AttributeModifier modifier = this.modifiers.remove(id);

    if (modifier != null) {
      this.persistentModifiers.remove(modifier.id(), modifier);
      Collection<AttributeModifier> modifiers = this.getModifiersByOperation(modifier.operation());
      List<AttributeModifier> ops = new ArrayList<>(modifiers);

      for (AttributeModifier op : ops) {

        if (op.id().equals(id)) {
          modifiers.remove(op);
        }
      }
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
    Set<ResourceLocation> ids = new HashSet<>(this.modifiers.keySet());

    for (ResourceLocation id : ids) {
      this.removeModifier(id);
    }
  }

  public void clearCachedModifiers() {

    for (AttributeModifier cachedModifier : this.cachedModifiers.values()) {
      this.removeModifier(cachedModifier.id());
    }
    this.cachedModifiers.clear();
    this.flagUpdate();

    if (this.itemHandler != null
        && this.itemHandler.getWearer() instanceof LivingEntity livingEntity) {
      this.clearCacheTick = livingEntity.tickCount;
    } else {
      this.clearCacheTick = -1;
    }
  }

  public void update() {

    if (this.update) {

      if (this.itemHandler == null
          || !(this.itemHandler.getWearer() instanceof LivingEntity livingEntity)
          || this.clearCacheTick == livingEntity.tickCount) {
        return;
      }
      this.update = false;
      double baseSize = this.baseSize;

      for (AttributeModifier mod :
          this.getModifiersByOperation(AttributeModifier.Operation.ADD_VALUE)) {
        baseSize += mod.amount();
      }
      double size = baseSize;

      for (AttributeModifier mod :
          this.getModifiersByOperation(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
        size += this.baseSize * mod.amount();
      }

      for (AttributeModifier mod :
          this.getModifiersByOperation(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
        size *= mod.amount();
      }

      size = Math.max(0, size);

      if (size != this.getSlots()) {
        this.resize((int) size);

        if (this.itemHandler.getWearer() != null) {
          NeoForge.EVENT_BUS.post(
              new SlotModifiersUpdatedEvent(this.itemHandler.getWearer(), Set.of(this.identifier)));

          if (this.itemHandler.getWearer() instanceof Player player
              && player.containerMenu instanceof ICuriosMenu curiosMenu) {
            curiosMenu.resetSlots();
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
        NonNullList<Boolean> newList = NonNullList.withSize(Math.max(0, newSize), true);

        for (int i = 0; i < newList.size() && i < this.renderHandler.size(); i++) {
          newList.set(i, renderHandler.get(i));
        }
        this.renderHandler = newList;
        newList = NonNullList.withSize(Math.max(0, newSize), true);

        for (int i = 0; i < newList.size() && i < this.activeStates.size(); i++) {
          newList.set(i, this.activeStates.get(i));
        }
        this.activeStates = newList;
        this.previousActiveStates = NonNullList.create();

        for (int i = 0; i < this.activeStates.size(); i++) {
          this.previousActiveStates.add(i, this.activeStates.get(i));
        }
      } else {
        this.stackHandler.grow(change);
        this.cosmeticStackHandler.grow(change);
        NonNullList<Boolean> newList = NonNullList.withSize(Math.max(0, newSize), true);

        for (int i = 0; i < newList.size() && i < this.renderHandler.size(); i++) {
          newList.set(i, renderHandler.get(i));
        }
        this.renderHandler = newList;
        newList = NonNullList.withSize(Math.max(0, newSize), true);

        for (int i = 0; i < newList.size() && i < this.activeStates.size(); i++) {
          newList.set(i, this.activeStates.get(i));
        }
        this.activeStates = newList;
        this.previousActiveStates = NonNullList.create();

        for (int i = 0; i < this.activeStates.size(); i++) {
          this.previousActiveStates.add(i, this.activeStates.get(i));
        }
      }
    }
  }

  private void loseStacks(IDynamicStackHandler stackHandler, String identifier, int amount) {

    if (this.itemHandler == null) {
      return;
    }
    List<ItemStack> drops = new ArrayList<>();

    for (int i = Math.max(0, stackHandler.getSlots() - amount);
         i >= 0 && i < stackHandler.getSlots();
         i++) {
      ItemStack stack = stackHandler.getStackInSlot(i);
      drops.add(stackHandler.getStackInSlot(i));
      LivingEntity entity = this.itemHandler.getWearer();
      SlotContext slotContext = new SlotContext(identifier, entity, i, false, this.visible);

      if (!stack.isEmpty()) {
        ResourceLocation id = CuriosApi.getSlotId(slotContext);
        ICurioItem
            .forEachModifier(stack, slotContext,
                             (attributeHolder, attributeModifier) -> {
                               if (attributeHolder.value() instanceof SlotAttribute slotAttribute) {
                                 this.itemHandler.removeSlotModifier(
                                     slotAttribute.id(),
                                     attributeModifier.id());
                               } else {
                                 AttributeInstance instance =
                                     entity.getAttributes().getInstance(attributeHolder);

                                 if (instance != null) {
                                   instance.removeModifier(attributeModifier);
                                 }
                               }
                             });
        CuriosApi.getCurio(stack).ifPresent(curio -> curio.onUnequip(slotContext, ItemStack.EMPTY));
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
