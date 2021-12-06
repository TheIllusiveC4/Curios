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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.api.type.util.ICuriosHelper;
import top.theillusivec4.curios.api.type.util.ISlotHelper;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;

public class CurioInventoryCapability {

  public static void register() {
    CapabilityManager.INSTANCE
        .register(ICuriosItemHandler.class, new Capability.IStorage<ICuriosItemHandler>() {
          @Override
          public INBT writeNBT(Capability<ICuriosItemHandler> capability,
                               ICuriosItemHandler instance, Direction side) {
            return instance.writeNBT();
          }

          @Override
          public void readNBT(Capability<ICuriosItemHandler> capability,
                              ICuriosItemHandler instance, Direction side, INBT nbt) {
            instance.readNBT((CompoundNBT) nbt);
          }
        }, CurioInventoryWrapper::new);
  }

  public static ICapabilityProvider createProvider(final PlayerEntity playerEntity) {
    return new Provider(playerEntity);
  }

  public static class CurioInventoryWrapper implements ICuriosItemHandler {
    Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
    Set<String> locked = new HashSet<>();
    NonNullList<ItemStack> invalidStacks = NonNullList.create();
    PlayerEntity wearer;
    Set<String> toLock = new HashSet<>();
    List<UnlockState> toUnlock = new ArrayList<>();
    Tuple<Integer, Integer> fortuneAndLooting = new Tuple<>(0, 0);
    Set<ICurioStacksHandler> updates = new HashSet<>();

    CurioInventoryWrapper() {
      this(null);
    }

    CurioInventoryWrapper(final PlayerEntity playerEntity) {
      this.wearer = playerEntity;
      this.reset();
    }

    @Override
    public void reset() {
      ISlotHelper slotHelper = CuriosApi.getSlotHelper();

      if (slotHelper != null && this.wearer != null && !this.wearer.getEntityWorld().isRemote()) {
        this.locked.clear();
        this.curios.clear();
        this.invalidStacks.clear();
        SortedSet<ISlotType> sorted = new TreeSet<>(slotHelper.getSlotTypes(this.wearer));

        for (ISlotType slotType : sorted) {
          this.curios.put(slotType.getIdentifier(),
              new CurioStacksHandler(this, slotType.getIdentifier(), slotType.getSize(), 0,
                  slotType.isVisible(), slotType.hasCosmetic()));
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
    public Set<String> getLockedSlots() {
      return Collections.unmodifiableSet(this.locked);
    }

    @Override
    public Optional<ICurioStacksHandler> getStacksHandler(String identifier) {
      return Optional.ofNullable(this.curios.get(identifier));
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
    public void unlockSlotType(String identifier, int amount, boolean visible, boolean cosmetic) {
      this.toUnlock.add(new UnlockState(identifier, amount, visible, cosmetic));
    }

    @Override
    public void lockSlotType(String identifier) {
      this.toLock.add(identifier);
    }

    @Override
    public void processSlots() {
      this.toLock.forEach(id -> this.getStacksHandler(id).ifPresent(stackHandler -> {
        this.curios.remove(id);
        this.locked.add(id);
        this.loseStacks(stackHandler.getStacks(), id, stackHandler.getSlots());
      }));
      this.toUnlock.forEach(state -> {
        this.curios.putIfAbsent(state.identifier,
            new CurioStacksHandler(this, state.identifier, state.amount, 0, state.visible,
                state.cosmetic));
        this.locked.remove(state.identifier);
      });
      this.toLock.clear();
      this.toUnlock.clear();
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
        this.invalidStacks.forEach(drop -> ItemHandlerHelper.giveItemToPlayer(this.wearer, drop));
        this.invalidStacks = NonNullList.create();
      }
    }

    private void loseStacks(IDynamicStackHandler stackHandler, String identifier, int amount) {

      if (this.wearer != null && !this.wearer.getEntityWorld().isRemote()) {
        List<ItemStack> drops = new ArrayList<>();

        for (int i = stackHandler.getSlots() - amount; i < stackHandler.getSlots(); i++) {
          ItemStack stack = stackHandler.getStackInSlot(i);
          drops.add(stackHandler.getStackInSlot(i));
          SlotContext slotContext = new SlotContext(identifier, this.wearer, i);

          if (!stack.isEmpty()) {
            UUID uuid = UUID.nameUUIDFromBytes((identifier + i).getBytes());
            this.wearer.getAttributeManager().removeModifiers(
                CuriosApi.getCuriosHelper().getAttributeModifiers(slotContext, uuid, stack));
            CuriosApi.getCuriosHelper().getCurio(stack)
                .ifPresent(curio -> curio.onUnequip(slotContext, ItemStack.EMPTY));
          }
          stackHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
        drops.forEach(drop -> ItemHandlerHelper.giveItemToPlayer(this.wearer, drop));
      }
    }

    public static class UnlockState {

      final String identifier;
      final int amount;
      final boolean visible;
      final boolean cosmetic;

      UnlockState(String identifier, int amount, boolean visible, boolean cosmetic) {
        this.identifier = identifier;
        this.amount = amount;
        this.visible = visible;
        this.cosmetic = cosmetic;
      }
    }

    @Override
    public int getFortuneBonus() {
      return this.fortuneAndLooting.getA();
    }

    @Override
    public int getLootingBonus() {
      return this.fortuneAndLooting.getB();
    }

    @Override
    public ListNBT saveInventory(boolean clear) {
      ListNBT taglist = new ListNBT();

      for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
        CompoundNBT tag = new CompoundNBT();
        ICurioStacksHandler stacksHandler = entry.getValue();
        IDynamicStackHandler stacks = stacksHandler.getStacks();
        IDynamicStackHandler cosmetics = stacksHandler.getCosmeticStacks();
        tag.put("Stacks", stacks.serializeNBT());
        tag.put("Cosmetics", cosmetics.serializeNBT());
        tag.putString("Identifier", entry.getKey());
        taglist.add(tag);

        if (clear) {

          for (int i = 0; i < stacks.getSlots(); i++) {
            stacks.setStackInSlot(i, ItemStack.EMPTY);
          }

          for (int i = 0; i < cosmetics.getSlots(); i++) {
            cosmetics.setStackInSlot(i, ItemStack.EMPTY);
          }
        }
      }
      return taglist;
    }

    @Override
    public void loadInventory(ListNBT data) {

      if (data != null) {

        for (int i = 0; i < data.size(); i++) {
          CompoundNBT tag = data.getCompound(i);
          String identifier = tag.getString("Identifier");
          ICurioStacksHandler stacksHandler = curios.get(identifier);

          if (stacksHandler != null) {
            CompoundNBT stacksData = tag.getCompound("Stacks");
            ItemStackHandler loaded = new ItemStackHandler();
            IDynamicStackHandler stacks = stacksHandler.getStacks();

            if (!stacksData.isEmpty()) {
              loaded.deserializeNBT(stacksData);
              loadStacks(stacksHandler, loaded, stacks);
            }
            stacksData = tag.getCompound("Cosmetics");

            if (!stacksData.isEmpty()) {
              loaded.deserializeNBT(stacksData);
              stacks = stacksHandler.getCosmeticStacks();
              loadStacks(stacksHandler, loaded, stacks);
            }
          }
        }
      }
    }

    private void loadStacks(ICurioStacksHandler stacksHandler, ItemStackHandler loaded,
                            IDynamicStackHandler stacks) {

      for (int j = 0; j < stacksHandler.getSlots() && j < loaded.getSlots(); j++) {
        ItemStack stack = stacks.getStackInSlot(j);
        ItemStack loadedStack = loaded.getStackInSlot(j);

        if (stack.isEmpty()) {
          stacks.setStackInSlot(j, loadedStack);
        } else {
          this.loseInvalidStack(stack);
        }
      }
    }

    @Override
    public void setEnchantmentBonuses(Tuple<Integer, Integer> fortuneAndLootingIn) {
      this.fortuneAndLooting = fortuneAndLootingIn;
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
            stacksHandler.removeModifier(attributeModifier.getID());
          }
        }
      }
    }

    @Override
    public void clearSlotModifiers() {

      for (Map.Entry<String, ICurioStacksHandler> entry : this.curios.entrySet()) {
        entry.getValue().clearModifiers();
      }
    }

    @Override
    public void clearCachedSlotModifiers() {

      for (Map.Entry<String, ICurioStacksHandler> entry : this.curios.entrySet()) {
        entry.getValue().clearCachedModifiers();
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

    @Override
    public CompoundNBT writeNBT() {
      CompoundNBT compound = new CompoundNBT();

      ListNBT taglist = new ListNBT();
      this.getCurios().forEach((key, stacksHandler) -> {
        CompoundNBT tag = new CompoundNBT();
        tag.put("StacksHandler", stacksHandler.serializeNBT());
        tag.putString("Identifier", key);
        taglist.add(tag);
      });
      compound.put("Curios", taglist);

      ListNBT taglist1 = new ListNBT();

      for (String identifier : this.getLockedSlots()) {
        taglist1.add(StringNBT.valueOf(identifier));
      }
      compound.put("Locked", taglist1);
      return compound;
    }

    @Override
    public void readNBT(CompoundNBT compoundNBT) {
      ListNBT tagList = compoundNBT.getList("Curios", NBT.TAG_COMPOUND);
      ListNBT lockedList = compoundNBT.getList("Locked", NBT.TAG_STRING);
      ISlotHelper slotHelper = CuriosApi.getSlotHelper();
      ICuriosHelper curiosHelper = CuriosApi.getCuriosHelper();
      LivingEntity livingEntity = this.getWearer();

      if (!tagList.isEmpty() && slotHelper != null) {
        Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
        SortedMap<ISlotType, ICurioStacksHandler> sortedCurios = new TreeMap<>();
        SortedSet<ISlotType> sorted =
            new TreeSet<>(CuriosApi.getSlotHelper().getSlotTypes(this.wearer));

        for (ISlotType slotType : sorted) {
          sortedCurios.put(slotType,
              new CurioStacksHandler(this, slotType.getIdentifier(), slotType.getSize(), 0,
                  slotType.isVisible(), slotType.hasCosmetic()));
        }

        for (int i = 0; i < tagList.size(); i++) {
          CompoundNBT tag = tagList.getCompound(i);
          String identifier = tag.getString("Identifier");
          CurioStacksHandler prevStacksHandler = new CurioStacksHandler(this, identifier);
          prevStacksHandler.deserializeNBT(tag.getCompound("StacksHandler"));

          Optional<ISlotType> optionalType = CuriosApi.getSlotHelper()
              .getSlotType(identifier);
          optionalType.ifPresent(type -> {
            CurioStacksHandler newStacksHandler =
                new CurioStacksHandler(this, type.getIdentifier(), type.getSize(),
                    prevStacksHandler.getSizeShift(), type.isVisible(), type.hasCosmetic());
            newStacksHandler.copyModifiers(prevStacksHandler);
            int index = 0;

            while (index < newStacksHandler.getSlots() && index < prevStacksHandler
                .getSlots()) {
              ItemStack prevStack = prevStacksHandler.getStacks().getStackInSlot(index);
              SlotContext slotContext = new SlotContext(identifier, livingEntity, index);

              if (!prevStack.isEmpty()) {

                if (curiosHelper.isStackValid(slotContext, prevStack)) {
                  newStacksHandler.getStacks().setStackInSlot(index, prevStack);
                } else {
                  this.loseInvalidStack(prevStack);
                }
              }
              ItemStack prevCosmetic =
                  prevStacksHandler.getCosmeticStacks().getStackInSlot(index);

              if (!prevCosmetic.isEmpty()) {

                if (curiosHelper.isStackValid(slotContext, prevCosmetic)) {
                  newStacksHandler.getCosmeticStacks().setStackInSlot(index,
                      prevStacksHandler.getCosmeticStacks().getStackInSlot(index));
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

          if (!optionalType.isPresent()) {
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

        for (int k = 0; k < lockedList.size(); k++) {
          this.lockSlotType(lockedList.getString(k));
        }
      }
    }
  }

  public static class Provider implements ICapabilitySerializable<INBT> {

    final LazyOptional<ICuriosItemHandler> optional;
    final ICuriosItemHandler handler;

    Provider(final PlayerEntity playerEntity) {
      this.handler = new CurioInventoryWrapper(playerEntity);
      this.optional = LazyOptional.of(() -> this.handler);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nullable Capability<T> capability, Direction facing) {
      return CuriosCapability.INVENTORY.orEmpty(capability, this.optional);
    }

    @Override
    public INBT serializeNBT() {
      return CuriosCapability.INVENTORY.writeNBT(this.handler, null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
      CuriosCapability.INVENTORY.readNBT(this.handler, null, nbt);
    }
  }
}
