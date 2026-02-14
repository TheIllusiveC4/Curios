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
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.CuriosConstants;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.event.SlotModifiersUpdatedEvent;
import top.theillusivec4.curios.api.type.ICuriosMenu;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.capability.CurioInventory;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncActiveState;
import top.theillusivec4.curios.impl.CuriosRegistry;

public class CurioStacksHandler implements ICurioStacksHandler {

  private final CurioInventory curioInventory;
  private final String identifier;
  private final Map<Identifier, AttributeModifier> modifiers = new HashMap<>();
  private final Map<Identifier, AttributeModifier> persistentModifiers = new HashMap<>();
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

  private boolean dataLoaded = false;

  public CurioStacksHandler(CurioInventory curioInventory, String identifier) {
    this(curioInventory, identifier, 1, true, false, true, DropRule.DEFAULT);
  }

  public CurioStacksHandler(
      CurioInventory curioInventory,
      String identifier,
      int size,
      boolean visible,
      boolean cosmetic,
      boolean canToggleRender,
      DropRule dropRule) {
    this.baseSize = size;
    this.visible = visible;
    this.cosmetic = cosmetic;
    this.curioInventory = curioInventory;
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
                    curioInventory.getOwner(),
                    index,
                    false,
                    this.getRenders().get(index)));
    this.cosmeticStackHandler =
        new DynamicStackHandler(
            size,
            (index) ->
                new SlotContext(
                    identifier,
                    curioInventory.getOwner(),
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
    LivingEntity livingEntity = this.curioInventory.getOwner();

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
    LivingEntity livingEntity = this.curioInventory.getOwner();
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
        new CurioChangeEvent.Item(livingEntity, slotContext, stack, ItemStack.EMPTY));
    AttributeMap attributeMap = livingEntity.getAttributes();

    if (!stack.isEmpty()) {
      ICurioItem
          .forEachModifier(stack, slotContext,
              (attributeHolder, attributeModifier) -> {
                if (attributeHolder.value() instanceof SlotAttribute slotAttribute
                    && slotAttribute.id().equals(this.identifier)) {
                  this.removeModifier(attributeModifier.id());
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
    LivingEntity livingEntity = this.curioInventory.getOwner();
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
        new CurioChangeEvent.Item(livingEntity, slotContext, ItemStack.EMPTY, stack));
    AttributeMap attributeMap = livingEntity.getAttributes();

    if (!stack.isEmpty()) {
      ICurioItem
          .forEachModifier(stack, slotContext,
              (attributeHolder, attributeModifier) -> {
                if (attributeHolder.value() instanceof SlotAttribute slotAttribute
                    && slotAttribute.id().equals(this.identifier)) {
                  this.addTransientModifier(
                      new AttributeModifier(attributeModifier.id(), attributeModifier.amount(),
                          attributeModifier.operation()));
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
  public int getBaseSize() {
    return this.baseSize;
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
  public CompoundTag serializeNBT() {
    LivingEntity livingEntity = this.curioInventory.getOwner();
    try (
        ProblemReporter.ScopedCollector problemreporter$scopedcollector =
            new ProblemReporter.ScopedCollector(livingEntity.problemPath(), CuriosConstants.LOG)) {
      TagValueOutput tagvalueoutput =
          TagValueOutput.createWithContext(problemreporter$scopedcollector,
              livingEntity.registryAccess());
      this.serialize(tagvalueoutput);
      return tagvalueoutput.buildResult();
    }
  }

  @Override
  public void deserializeNBT(CompoundTag nbt) {
    LivingEntity livingEntity = this.curioInventory.getOwner();
    try (
        ProblemReporter.ScopedCollector problemreporter$scopedcollector =
            new ProblemReporter.ScopedCollector(livingEntity.problemPath(), CuriosConstants.LOG)) {
      this.deserialize(
          TagValueInput.create(problemreporter$scopedcollector, livingEntity.registryAccess(),
              nbt));
    }
    this.flagUpdate();
  }

  @Override
  public String getIdentifier() {
    return this.identifier;
  }

  public CompoundTag getSyncTag() {
    LivingEntity livingEntity = this.curioInventory.getOwner();
    try (
        ProblemReporter.ScopedCollector problemreporter$scopedcollector =
            new ProblemReporter.ScopedCollector(livingEntity.problemPath(), CuriosConstants.LOG)) {
      TagValueOutput tagvalueoutput =
          TagValueOutput.createWithContext(problemreporter$scopedcollector,
              livingEntity.registryAccess());
      this.serialize(tagvalueoutput);
      CompoundTag tag = tagvalueoutput.buildResult();

      if (!this.modifiers.isEmpty()) {
        tag.remove("PermanentModifiers");
        tag.store("Modifiers", AttributeModifier.CODEC.listOf(),
            List.copyOf(this.modifiers.values()));
      }
      return tag;
    }
  }

  public void applySyncTag(CompoundTag tag) {
    LivingEntity livingEntity = this.curioInventory.getOwner();
    try (
        ProblemReporter.ScopedCollector problemreporter$scopedcollector =
            new ProblemReporter.ScopedCollector(livingEntity.problemPath(), CuriosConstants.LOG)) {
      this.deserialize(
          TagValueInput.create(problemreporter$scopedcollector, livingEntity.registryAccess(),
              tag));
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
    }
    this.flagUpdate();
    this.update();
  }

  @Override
  public void copyModifiers(ICurioStacksHandler other) {
    this.modifiers.clear();
    this.modifiersByOperation.clear();
    this.persistentModifiers.clear();
    other.getModifiers().forEach((uuid, modifier) -> this.addTransientModifier(modifier));

    for (AttributeModifier persistentModifier : other.getPermanentModifiers()) {
      this.addPermanentModifier(persistentModifier);
    }
    this.update();
  }

  public Map<Identifier, AttributeModifier> getModifiers() {
    return this.modifiers;
  }

  @Override
  public Set<AttributeModifier> getPermanentModifiers() {
    return new HashSet<>(this.persistentModifiers.values());
  }

  @Override
  public Set<AttributeModifier> getCachedModifiers() {
    return new HashSet<>();
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

  public void removeModifier(Identifier id) {
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
    this.curioInventory.getUpdatingInventories().remove(this);
    this.curioInventory.getUpdatingInventories().add(this);
  }

  public void clearModifiers() {
    Set<Identifier> ids = new HashSet<>(this.modifiers.keySet());

    for (Identifier id : ids) {
      this.removeModifier(id);
    }
  }

  public void clearCachedModifiers() {
    // NO-OP
  }

  public void setDataLoaded() {
    this.dataLoaded = true;
  }

  public void update() {

    if (this.update && this.dataLoaded) {
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
        LivingEntity livingEntity = this.curioInventory.getOwner();
        NeoForge.EVENT_BUS.post(
            new SlotModifiersUpdatedEvent(livingEntity, Set.of(this.identifier)));

        if (livingEntity instanceof Player player
            && player.containerMenu instanceof ICuriosMenu curiosMenu) {
          curiosMenu.resetSlots();
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
    List<ItemStack> drops = new ArrayList<>();

    for (int i = Math.max(0, stackHandler.getSlots() - amount);
         i >= 0 && i < stackHandler.getSlots();
         i++) {
      ItemStack stack = stackHandler.getStackInSlot(i);
      drops.add(stackHandler.getStackInSlot(i));
      LivingEntity entity = this.curioInventory.getOwner();
      SlotContext slotContext = new SlotContext(identifier, entity, i, false, this.visible);

      if (!stack.isEmpty()) {
        ICurioItem
            .forEachModifier(stack, slotContext,
                (attributeHolder, attributeModifier) -> {
                  if (attributeHolder.value() instanceof SlotAttribute slotAttribute
                      && slotAttribute.id().equals(identifier)) {
                    this.removeModifier(attributeModifier.id());
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
    drops.forEach(stack -> this.curioInventory.getDroppingStacks().add(stack));
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

  @Override
  public void serialize(@Nonnull ValueOutput output) {
    output.putString("Identifier", this.getIdentifier());
    output.putInt("BaseSize", this.getBaseSize());
    output.putInt("Size", this.getSlots());
    output.putChild("Stacks", this.getStacks());
    output.putChild("Cosmetics", this.getCosmeticStacks());
    output.store("Renders", Codec.BOOL.listOf(), this.getRenders());
    output.store("ActiveStates", Codec.BOOL.listOf(), this.getActiveStates());
    output.putBoolean("Cosmetic", this.hasCosmetic());
    output.putBoolean("Visible", this.isVisible());
    output.putBoolean("ToggleRender", this.canToggleRendering());
    output.store("DropRule", DropRule.CODEC, this.getDropRule());
    Set<AttributeModifier> permanentModifiers = this.getPermanentModifiers();
    output.store("PermanentModifiers", AttributeModifier.CODEC.listOf(),
        new ArrayList<>(permanentModifiers));
  }

  @Override
  public void deserialize(@Nonnull ValueInput input) {
    this.baseSize = input.getIntOr("BaseSize", this.getBaseSize());
    this.resize(input.getIntOr("Size", this.getSlots()));
    input.child("Stacks").ifPresent(stacks -> this.getStacks().deserialize(stacks));
    input.child("Cosmetics")
        .ifPresent(cosmetics -> this.getCosmeticStacks().deserialize(cosmetics));
    List<Boolean> renders = input.read("Renders", Codec.BOOL.listOf()).orElse(List.of());
    List<Boolean> currentRenders = this.getRenders();

    for (int i = 0; i < renders.size(); i++) {
      currentRenders.set(i, renders.get(i));
    }
    List<Boolean> activeStates = input.read("ActiveStates", Codec.BOOL.listOf()).orElse(List.of());
    List<Boolean> currentActiveStates = this.getActiveStates();

    for (int i = 0; i < activeStates.size(); i++) {
      currentActiveStates.set(i, activeStates.get(i));
    }
    this.cosmetic = input.getBooleanOr("Cosmetic", this.hasCosmetic());
    this.visible = input.getBooleanOr("Visible", this.isVisible());
    this.canToggleRender = input.getBooleanOr("ToggleRender", this.canToggleRendering());
    this.dropRule = input.read("DropRule", DropRule.CODEC).orElse(this.getDropRule());
    List<AttributeModifier> permanentModifiers =
        input.read("PermanentModifiers", AttributeModifier.CODEC.listOf()).orElse(List.of());

    for (AttributeModifier modifier : permanentModifiers) {
      this.addPermanentModifier(modifier);
    }
    List<AttributeModifier> modifiers =
        input.read("Modifiers", AttributeModifier.CODEC.listOf()).orElse(List.of());

    for (AttributeModifier modifier : modifiers) {
      this.addTransientModifier(modifier);
    }
  }
}
