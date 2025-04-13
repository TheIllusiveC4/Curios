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

package top.theillusivec4.curios.common.capability;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;

public class CurioInventoryCapability {

  public static ICapabilityProvider createProvider(final LivingEntity livingEntity) {
    return new Provider(livingEntity);
  }

  public static class CurioInventoryWrapper implements ICuriosItemHandler {

    Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
    NonNullList<ItemStack> invalidStacks = NonNullList.create();
    LivingEntity wearer;
    Set<ICurioStacksHandler> updates = new HashSet<>();

    public CurioInventoryWrapper(final LivingEntity livingEntity) {
      this.wearer = livingEntity;
      this.reset();
    }

    @Override
    public void reset() {

      if (this.wearer != null) {
        this.curios.clear();
        this.invalidStacks.clear();
        SortedSet<ISlotType> sorted =
            new TreeSet<>(CuriosApi.getEntitySlots(this.wearer).values());

        for (ISlotType slotType : sorted) {
          this.curios.put(slotType.getIdentifier(),
                          new CurioStacksHandler(this, slotType.getIdentifier(), slotType.getSize(),
                                                 slotType.useNativeGui(), slotType.hasCosmetic(),
                                                 slotType.canToggleRendering(),
                                                 slotType.getDropRule()));
        }
      }
    }

    @Override
    public int getSlots() {
      int totalSlots = 0;

      for (ICurioStacksHandler stacks : this.curios.values()) {
        totalSlots += stacks.getSlots();
      }
      return totalSlots;
    }

    @Override
    public int getVisibleSlots() {
      int totalSlots = 0;

      for (ICurioStacksHandler stacks : this.curios.values()) {

        if (stacks.isVisible()) {
          totalSlots += stacks.getSlots();
        }
      }
      return totalSlots;
    }

    @Override
    public Optional<ICurioStacksHandler> getStacksHandler(String identifier) {
      return Optional.ofNullable(this.curios.get(identifier));
    }

    @Override
    public IItemHandlerModifiable getEquippedCurios() {
      Map<String, ICurioStacksHandler> curios = this.getCurios();
      IItemHandlerModifiable[] itemHandlers = new IItemHandlerModifiable[curios.size()];
      int index = 0;

      for (ICurioStacksHandler stacksHandler : curios.values()) {

        if (index < itemHandlers.length) {
          itemHandlers[index] = stacksHandler.getStacks();
          index++;
        }
      }
      return new CombinedInvWrapper(itemHandlers);
    }

    @Override
    public void setEquippedCurio(String identifier, int index, ItemStack stack) {
      Map<String, ICurioStacksHandler> curios = this.getCurios();
      ICurioStacksHandler stacksHandler = curios.get(identifier);

      if (stacksHandler != null) {
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        if (index < stackHandler.getSlots()) {
          stackHandler.setStackInSlot(index, stack);
        }
      }
    }

    @Override
    public Optional<SlotResult> findFirstCurio(Item item) {
      return findFirstCurio(stack -> stack.getItem() == item);
    }

    @Override
    public Optional<SlotResult> findFirstCurio(Predicate<ItemStack> filter) {
      Map<String, ICurioStacksHandler> curios = this.getCurios();

      for (String id : curios.keySet()) {
        ICurioStacksHandler stacksHandler = curios.get(id);
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        for (int i = 0; i < stackHandler.getSlots(); i++) {
          ItemStack stack = stackHandler.getStackInSlot(i);

          if (!stack.isEmpty() && filter.test(stack)) {
            NonNullList<Boolean> renderStates = stacksHandler.getRenders();
            return Optional.of(new SlotResult(new SlotContext(id, this.wearer, i, false,
                                                              renderStates.size() > i
                                                                  && renderStates.get(i)), stack));
          }
        }
      }
      return Optional.empty();
    }

    @Override
    public List<SlotResult> findCurios(Item item) {
      return findCurios(stack -> stack.getItem() == item);
    }

    @Override
    public List<SlotResult> findCurios(Predicate<ItemStack> filter) {
      List<SlotResult> result = new ArrayList<>();
      Map<String, ICurioStacksHandler> curios = this.getCurios();

      for (String id : curios.keySet()) {
        ICurioStacksHandler stacksHandler = curios.get(id);
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        for (int i = 0; i < stackHandler.getSlots(); i++) {
          ItemStack stack = stackHandler.getStackInSlot(i);

          if (!stack.isEmpty() && filter.test(stack)) {
            NonNullList<Boolean> renderStates = stacksHandler.getRenders();
            result.add(new SlotResult(new SlotContext(id, this.wearer, i, false,
                                                      renderStates.size() > i && renderStates.get(
                                                          i)), stack));
          }
        }
      }
      return result;
    }

    @Override
    public List<SlotResult> findCurios(String... identifiers) {
      List<SlotResult> result = new ArrayList<>();
      Set<String> ids = new HashSet<>(List.of(identifiers));
      Map<String, ICurioStacksHandler> curios = this.getCurios();

      for (String id : curios.keySet()) {

        if (ids.contains(id)) {
          ICurioStacksHandler stacksHandler = curios.get(id);
          IDynamicStackHandler stackHandler = stacksHandler.getStacks();

          for (int i = 0; i < stackHandler.getSlots(); i++) {
            ItemStack stack = stackHandler.getStackInSlot(i);

            if (!stack.isEmpty()) {
              NonNullList<Boolean> renderStates = stacksHandler.getRenders();
              result.add(new SlotResult(new SlotContext(id, this.wearer, i, false,
                                                        renderStates.size() > i && renderStates.get(
                                                            i)), stack));
            }
          }
        }
      }
      return result;
    }

    @Override
    public Optional<SlotResult> findCurio(String identifier, int index) {
      Map<String, ICurioStacksHandler> curios = this.getCurios();
      ICurioStacksHandler stacksHandler = curios.get(identifier);

      if (stacksHandler != null) {
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        if (index < stackHandler.getSlots()) {
          ItemStack stack = stackHandler.getStackInSlot(index);

          if (!stack.isEmpty()) {
            NonNullList<Boolean> renderStates = stacksHandler.getRenders();
            return Optional.of(new SlotResult(new SlotContext(identifier, this.wearer, index, false,
                                                              renderStates.size() > index
                                                                  && renderStates.get(index)),
                                              stack));
          }
        }
      }
      return Optional.empty();
    }

    @Override
    public Map<String, ICurioStacksHandler> getCurios() {
      return Collections.unmodifiableMap(this.curios);
    }

    @Override
    public void setCurios(Map<String, ICurioStacksHandler> curios) {
      this.curios = curios;
    }

    @Override
    public void growSlotType(String identifier, int amount) {

      if (amount > 0) {
        this.getStacksHandler(identifier).ifPresent(stackHandler -> stackHandler.grow(amount));
      }
    }

    @Override
    public void shrinkSlotType(String identifier, int amount) {

      if (amount > 0) {
        this.getStacksHandler(identifier).ifPresent(stackHandler -> stackHandler.shrink(amount));
      }
    }

    @Nullable
    @Override
    public LivingEntity getWearer() {
      return this.wearer;
    }

    @Override
    public void loseInvalidStack(ItemStack stack) {
      this.invalidStacks.add(stack);
    }

    @Override
    public void handleInvalidStacks() {

      if (this.wearer != null && !this.invalidStacks.isEmpty()) {

        if (this.wearer instanceof Player player) {
          this.invalidStacks.forEach(drop -> ItemHandlerHelper.giveItemToPlayer(player, drop));
        } else {
          this.invalidStacks.forEach(drop -> {
            ItemEntity ent = this.wearer.spawnAtLocation(drop, 1.0F);
            RandomSource rand = this.wearer.getRandom();

            if (ent != null) {
              ent.setDeltaMovement(ent.getDeltaMovement()
                                       .add((rand.nextFloat() - rand.nextFloat()) * 0.1F,
                                            rand.nextFloat() * 0.05F,
                                            (rand.nextFloat() - rand.nextFloat()) * 0.1F));
            }
          });
        }
        this.invalidStacks = NonNullList.create();
      }
    }

    @Override
    public int getFortuneLevel(@Nullable LootContext lootContext) {
      int fortuneLevel = 0;
      for (Map.Entry<String, ICurioStacksHandler> entry : getCurios().entrySet()) {
        IDynamicStackHandler stacks = entry.getValue().getStacks();

        for (int i = 0; i < stacks.getSlots(); i++) {
          final int index = i;
          fortuneLevel += CuriosApi.getCurio(stacks.getStackInSlot(i)).map(
              curio -> {
                NonNullList<Boolean> renderStates = entry.getValue().getRenders();
                return curio.getFortuneLevel(
                    new SlotContext(entry.getKey(), this.wearer, index, false,
                                    renderStates.size() > index && renderStates.get(index)),
                    lootContext);
              }).orElse(0);
        }
      }
      return fortuneLevel;
    }

    @Override
    public int getLootingLevel(DamageSource source, LivingEntity target, int baseLooting) {
      int lootingLevel = 0;
      for (Map.Entry<String, ICurioStacksHandler> entry : getCurios().entrySet()) {
        IDynamicStackHandler stacks = entry.getValue().getStacks();

        for (int i = 0; i < stacks.getSlots(); i++) {
          int index = i;
          lootingLevel += CuriosApi.getCurio(stacks.getStackInSlot(i)).map(
                  curio -> {
                    NonNullList<Boolean> renderStates = entry.getValue().getRenders();
                    return curio.getLootingLevel(
                        new SlotContext(entry.getKey(), this.wearer, index, false,
                                        renderStates.size() > index && renderStates.get(index)), source,
                        target,
                        baseLooting);
                  })
              .orElse(0);
        }
      }
      return lootingLevel;
    }

    @Override
    public ListTag saveInventory(boolean clear) {
      return this.saveInventory(clear, stack -> true);
    }

    @Override
    public ListTag saveInventory(boolean clear, Predicate<ItemStack> filter) {
      return this.saveInventory(clear, (stack, slotContext) -> filter.test(stack));
    }

    @Override
    public ListTag saveInventory(boolean clear, BiPredicate<ItemStack, SlotContext> filter) {
      ListTag taglist = new ListTag();

      for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
        CompoundTag tag = new CompoundTag();
        ICurioStacksHandler stacksHandler = entry.getValue();
        IDynamicStackHandler stacks = stacksHandler.getStacks();
        IDynamicStackHandler cosmetics = stacksHandler.getCosmeticStacks();
        String id = entry.getKey();
        NonNullList<Boolean> renders = stacksHandler.getRenders();
        tag.put("Stacks", getFilteredStacksTag(id, false, renders, stacks, filter, clear));
        tag.put("Cosmetics", getFilteredStacksTag(id, true, renders, cosmetics, filter, clear));
        tag.putString("Identifier", entry.getKey());
        Map<UUID, AttributeModifier> modifiers = stacksHandler.getModifiers();

        if (!modifiers.isEmpty()) {
          ListTag list = new ListTag();
          modifiers.forEach((uuid, modifier) -> {
            if (!stacksHandler.getPermanentModifiers().contains(modifier)) {
              list.add(modifier.save());
            }
          });
          tag.put("Modifiers", list);
        }
        taglist.add(tag);
      }
      return taglist;
    }

    protected CompoundTag getFilteredStacksTag(String id, boolean cosmetic,
                                               NonNullList<Boolean> renders,
                                               IDynamicStackHandler stacks,
                                               BiPredicate<ItemStack, SlotContext> filter,
                                               boolean clear) {
      ListTag nbtTagList = new ListTag();

      for (int i = 0; i < stacks.getSlots(); ++i) {
        ItemStack stack = stacks.getStackInSlot(i);
        SlotContext slotContext =
            new SlotContext(id, this.wearer, i, cosmetic, renders.size() < i && renders.get(i));

        if (!stack.isEmpty() && filter.test(stack, slotContext)) {
          CompoundTag itemTag = new CompoundTag();
          itemTag.putInt("Slot", i);
          stack.save(itemTag);
          nbtTagList.add(itemTag);

          if (clear) {
            stacks.setStackInSlot(i, ItemStack.EMPTY);
          }
        }
      }
      CompoundTag nbt = new CompoundTag();
      nbt.put("Items", nbtTagList);
      nbt.putInt("Size", stacks.getSlots());
      return nbt;
    }

    @Override
    public void loadInventory(ListTag data) {

      if (data != null) {

        for (int i = 0; i < data.size(); i++) {
          CompoundTag tag = data.getCompound(i);
          String identifier = tag.getString("Identifier");
          ICurioStacksHandler stacksHandler = curios.get(identifier);
          ItemStackHandler loaded = new ItemStackHandler();

          if (stacksHandler != null) {

            if (tag.contains("Modifiers", Tag.TAG_LIST)) {
              ListTag list = tag.getList("Modifiers", 10);

              for (int j = 0; j < list.size(); ++j) {
                AttributeModifier attributeModifier = AttributeModifier.load(list.getCompound(j));

                if (attributeModifier != null) {
                  stacksHandler.getCachedModifiers().add(attributeModifier);
                  stacksHandler.addTransientModifier(attributeModifier);
                }
              }
            }
          }
          CompoundTag stacksData = tag.getCompound("Stacks");

          if (!stacksData.isEmpty()) {
            loaded.deserializeNBT(stacksData);

            if (stacksHandler != null) {
              IDynamicStackHandler stacks = stacksHandler.getStacks();
              loadStacks(stacksHandler, loaded, stacks);
            } else {

              for (int i1 = 0; i1 < loaded.getSlots(); i1++) {
                this.loseInvalidStack(loaded.getStackInSlot(i1));
              }
            }
          }
          stacksData = tag.getCompound("Cosmetics");

          if (!stacksData.isEmpty()) {
            loaded.deserializeNBT(stacksData);

            if (stacksHandler != null) {
              IDynamicStackHandler stacks = stacksHandler.getCosmeticStacks();
              loadStacks(stacksHandler, loaded, stacks);
            } else {

              for (int i1 = 0; i1 < loaded.getSlots(); i1++) {
                this.loseInvalidStack(loaded.getStackInSlot(i1));
              }
            }
          }
        }
      }
    }

    @Override
    public Set<ICurioStacksHandler> getUpdatingInventories() {
      return this.updates;
    }

    @Override
    public void addTransientSlotModifiers(Multimap<String, AttributeModifier> modifiers) {

      for (Map.Entry<String, Collection<AttributeModifier>> entry : modifiers.asMap().entrySet()) {
        String id = entry.getKey();

        for (AttributeModifier attributeModifier : entry.getValue()) {
          ICurioStacksHandler stacksHandler = this.curios.get(id);

          if (stacksHandler != null) {
            stacksHandler.addTransientModifier(attributeModifier);
          }
        }
      }
    }

    @Override
    public void addPermanentSlotModifiers(Multimap<String, AttributeModifier> modifiers) {

      for (Map.Entry<String, Collection<AttributeModifier>> entry : modifiers.asMap().entrySet()) {
        String id = entry.getKey();

        for (AttributeModifier attributeModifier : entry.getValue()) {
          ICurioStacksHandler stacksHandler = this.curios.get(id);

          if (stacksHandler != null) {
            stacksHandler.addPermanentModifier(attributeModifier);
          }
        }
      }
    }

    @Override
    public void removeSlotModifiers(Multimap<String, AttributeModifier> modifiers) {

      for (Map.Entry<String, Collection<AttributeModifier>> entry : modifiers.asMap().entrySet()) {
        String id = entry.getKey();

        for (AttributeModifier attributeModifier : entry.getValue()) {
          ICurioStacksHandler stacksHandler = this.curios.get(id);

          if (stacksHandler != null) {
            stacksHandler.removeModifier(attributeModifier.getId());
          }
        }
      }
    }

    @Override
    public void addTransientSlotModifier(String slot, UUID uuid, String name, double amount,
                                         AttributeModifier.Operation operation) {
      Multimap<String, AttributeModifier> map = LinkedHashMultimap.create();
      map.put(slot, new AttributeModifier(uuid, name, amount, operation));
      this.addTransientSlotModifiers(map);
    }

    @Override
    public void addPermanentSlotModifier(String slot, UUID uuid, String name, double amount,
                                         AttributeModifier.Operation operation) {
      Multimap<String, AttributeModifier> map = LinkedHashMultimap.create();
      map.put(slot, new AttributeModifier(uuid, name, amount, operation));
      this.addPermanentSlotModifiers(map);
    }

    @Override
    public void removeSlotModifier(String slot, UUID uuid) {
      Multimap<String, AttributeModifier> map = LinkedHashMultimap.create();
      map.put(slot, new AttributeModifier(uuid, "", 0, AttributeModifier.Operation.ADDITION));
      this.removeSlotModifiers(map);
    }

    @Override
    public void clearSlotModifiers() {

      for (Map.Entry<String, ICurioStacksHandler> entry : this.curios.entrySet()) {
        entry.getValue().clearModifiers();
      }
    }

    @Override
    public void clearCachedSlotModifiers() {
      Multimap<String, AttributeModifier> slots = HashMultimap.create();

      for (Map.Entry<String, ICurioStacksHandler> entry : this.curios.entrySet()) {
        ICurioStacksHandler stacksHandler = entry.getValue();
        Set<AttributeModifier> modifiers = stacksHandler.getCachedModifiers();

        if (!modifiers.isEmpty()) {
          IDynamicStackHandler stacks = stacksHandler.getStacks();
          NonNullList<Boolean> renderStates = stacksHandler.getRenders();
          String id = entry.getKey();

          for (int i = 0; i < stacks.getSlots(); i++) {
            ItemStack stack = stacks.getStackInSlot(i);

            if (!stack.isEmpty()) {
              SlotContext slotContext = new SlotContext(id, this.getWearer(), i, false,
                                                        renderStates.size() > i && renderStates.get(
                                                            i));
              UUID uuid = CuriosApi.getSlotUuid(slotContext);
              Multimap<Attribute, AttributeModifier> map =
                  CuriosApi.getAttributeModifiers(slotContext, uuid, stack);

              for (Attribute attribute : map.keySet()) {

                if (attribute instanceof SlotAttribute wrapper) {
                  slots.putAll(wrapper.getIdentifier(), map.get(attribute));
                }
              }
            }
          }
        }
      }

      for (Map.Entry<String, Collection<AttributeModifier>> entry : slots.asMap().entrySet()) {
        String id = entry.getKey();
        ICurioStacksHandler stacksHandler = this.curios.get(id);

        if (stacksHandler != null) {

          for (AttributeModifier attributeModifier : entry.getValue()) {
            stacksHandler.getCachedModifiers().remove(attributeModifier);
          }
          stacksHandler.clearCachedModifiers();
        }
      }
    }

    @Override
    public Multimap<String, AttributeModifier> getModifiers() {
      Multimap<String, AttributeModifier> result = HashMultimap.create();

      for (Map.Entry<String, ICurioStacksHandler> entry : this.curios.entrySet()) {
        result.putAll(entry.getKey(), entry.getValue().getModifiers().values());
      }
      return result;
    }

    private void loadStacks(ICurioStacksHandler stacksHandler, ItemStackHandler loaded,
                            IDynamicStackHandler stacks) {
      int index = 0;

      while (index < stacksHandler.getSlots() && index < loaded.getSlots()) {
        ItemStack stack = stacks.getStackInSlot(index);
        ItemStack loadedStack = loaded.getStackInSlot(index);

        if (stack.isEmpty()) {
          stacks.setStackInSlot(index, loadedStack);
        } else {
          this.loseInvalidStack(loadedStack);
        }
        index++;
      }

      while (index < loaded.getSlots()) {
        this.loseInvalidStack(loaded.getStackInSlot(index));
        index++;
      }
    }

    @Override
    public Tag writeTag() {
      CompoundTag compound = new CompoundTag();

      ListTag taglist = new ListTag();
      this.getCurios().forEach((key, stacksHandler) -> {
        CompoundTag tag = new CompoundTag();
        tag.put("StacksHandler", stacksHandler.serializeNBT());
        tag.putString("Identifier", key);
        taglist.add(tag);
      });
      compound.put("Curios", taglist);
      return compound;
    }

    @Override
    public void readTag(Tag nbt) {
      ListTag tagList = ((CompoundTag) nbt).getList("Curios", Tag.TAG_COMPOUND);
      LivingEntity livingEntity = this.getWearer();

      if (!tagList.isEmpty()) {
        Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
        SortedMap<ISlotType, ICurioStacksHandler> sortedCurios = new TreeMap<>();
        SortedSet<ISlotType> sorted =
            new TreeSet<>(CuriosApi.getEntitySlots(this.wearer).values());

        for (ISlotType slotType : sorted) {
          sortedCurios.put(slotType,
                           new CurioStacksHandler(this, slotType.getIdentifier(),
                                                  slotType.getSize(),
                                                  slotType.useNativeGui(), slotType.hasCosmetic(),
                                                  slotType.canToggleRendering(),
                                                  slotType.getDropRule()));
        }

        for (int i = 0; i < tagList.size(); i++) {
          CompoundTag tag = tagList.getCompound(i);
          String identifier = tag.getString("Identifier");
          CurioStacksHandler prevStacksHandler = new CurioStacksHandler(this, identifier);
          prevStacksHandler.deserializeNBT(tag.getCompound("StacksHandler"));

          Optional<ISlotType> optionalType =
              Optional.ofNullable(CuriosApi.getEntitySlots(this.wearer).get(identifier));
          optionalType.ifPresent(type -> {
            CurioStacksHandler newStacksHandler =
                new CurioStacksHandler(this, type.getIdentifier(), type.getSize(),
                                       type.useNativeGui(), type.hasCosmetic(),
                                       type.canToggleRendering(),
                                       type.getDropRule());
            newStacksHandler.copyModifiers(prevStacksHandler);
            int index = 0;

            while (index < newStacksHandler.getSlots() && index < prevStacksHandler
                .getSlots()) {
              ItemStack prevStack = prevStacksHandler.getStacks().getStackInSlot(index);

              if (!prevStack.isEmpty()) {

                if (newStacksHandler.getStacks().isItemValid(index, prevStack)) {
                  newStacksHandler.getStacks().setStackInSlot(index, prevStack);
                } else {
                  this.loseInvalidStack(prevStack);
                }
              }
              ItemStack prevCosmetic =
                  prevStacksHandler.getCosmeticStacks().getStackInSlot(index);

              if (!prevCosmetic.isEmpty()) {

                if (newStacksHandler.getStacks().isItemValid(index, prevCosmetic)) {
                  newStacksHandler.getCosmeticStacks().setStackInSlot(index,
                                                                      prevStacksHandler.getCosmeticStacks()
                                                                          .getStackInSlot(index));
                } else {
                  this.loseInvalidStack(prevCosmetic);
                }
              }
              index++;
            }

            while (index < prevStacksHandler.getSlots()) {
              this.loseInvalidStack(prevStacksHandler.getStacks().getStackInSlot(index));
              this.loseInvalidStack(
                  prevStacksHandler.getCosmeticStacks().getStackInSlot(index));
              index++;
            }
            sortedCurios.put(type, newStacksHandler);

            for (int j = 0;
                 j < newStacksHandler.getRenders().size() &&
                     j < prevStacksHandler.getRenders()
                         .size(); j++) {
              newStacksHandler.getRenders().set(j, prevStacksHandler.getRenders().get(j));
            }
          });

          if (optionalType.isEmpty()) {
            IDynamicStackHandler stackHandler = prevStacksHandler.getStacks();
            IDynamicStackHandler cosmeticStackHandler = prevStacksHandler.getCosmeticStacks();

            for (int j = 0; j < stackHandler.getSlots(); j++) {
              ItemStack stack = stackHandler.getStackInSlot(j);

              if (!stack.isEmpty()) {
                this.loseInvalidStack(stack);
              }

              ItemStack cosmeticStack = cosmeticStackHandler.getStackInSlot(j);

              if (!cosmeticStack.isEmpty()) {
                this.loseInvalidStack(cosmeticStack);
              }
            }
          }
        }
        sortedCurios.forEach(
            (slotType, stacksHandler) -> curios.put(slotType.getIdentifier(), stacksHandler));
        this.setCurios(curios);
      }
    }
  }

  public static class Provider implements ICapabilitySerializable<Tag> {

    final LazyOptional<ICuriosItemHandler> optional;
    final ICuriosItemHandler handler;
    final LivingEntity wearer;

    Provider(final LivingEntity livingEntity) {
      this.wearer = livingEntity;
      this.handler = new CurioInventoryWrapper(this.wearer);
      this.optional = LazyOptional.of(() -> this.handler);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nullable Capability<T> capability, Direction facing) {

      if (CuriosApi.getEntitySlots(this.wearer).isEmpty()) {
        return LazyOptional.empty();
      }
      return CuriosCapability.INVENTORY.orEmpty(capability, this.optional);
    }

    @Override
    public Tag serializeNBT() {
      return this.handler.writeTag();
    }

    @Override
    public void deserializeNBT(Tag nbt) {
      this.handler.readTag(nbt);
    }
  }
}
