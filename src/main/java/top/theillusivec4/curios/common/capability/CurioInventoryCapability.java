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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.api.type.util.ISlotHelper;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;

public class CurioInventoryCapability {

  public static void register() {
    CapabilityManager.INSTANCE
        .register(ICuriosItemHandler.class, new Capability.IStorage<ICuriosItemHandler>() {
          @Override
          public INBT writeNBT(Capability<ICuriosItemHandler> capability,
              ICuriosItemHandler instance, Direction side) {
            CompoundNBT compound = new CompoundNBT();

            ListNBT taglist = new ListNBT();
            instance.getCurios().forEach((key, stacksHandler) -> {
              CompoundNBT tag = new CompoundNBT();
              tag.put("StacksHandler", stacksHandler.serializeNBT());
              tag.putString("Identifier", key);
              taglist.add(tag);
            });
            compound.put("Curios", taglist);

            ListNBT taglist1 = new ListNBT();

            for (String identifier : instance.getLockedSlots()) {
              taglist1.add(StringNBT.valueOf(identifier));
            }
            compound.put("Locked", taglist1);
            return compound;
          }

          @Override
          public void readNBT(Capability<ICuriosItemHandler> capability,
              ICuriosItemHandler instance, Direction side, INBT nbt) {
            ListNBT tagList = ((CompoundNBT) nbt).getList("Curios", NBT.TAG_COMPOUND);
            ListNBT lockedList = ((CompoundNBT) nbt).getList("Locked", NBT.TAG_STRING);
            ISlotHelper slotHelper = CuriosApi.getSlotHelper();

            if (!tagList.isEmpty() && slotHelper != null) {
              Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
              SortedMap<ISlotType, ICurioStacksHandler> sortedCurios = slotHelper.createSlots();

              for (int i = 0; i < tagList.size(); i++) {
                CompoundNBT tag = tagList.getCompound(i);
                String identifier = tag.getString("Identifier");
                CurioStacksHandler prevStacksHandler = new CurioStacksHandler();
                prevStacksHandler.deserializeNBT(tag.getCompound("StacksHandler"));

                Optional<ISlotType> optionalType = CuriosApi.getSlotHelper()
                    .getSlotType(identifier);
                optionalType.ifPresent(type -> {
                  CurioStacksHandler newStacksHandler = new CurioStacksHandler(type.getSize(),
                      prevStacksHandler.getSizeShift(), type.isVisible(), type.hasCosmetic());
                  int index = 0;

                  while (index < newStacksHandler.getSlots() && index < prevStacksHandler
                      .getSlots()) {
                    newStacksHandler.getStacks()
                        .setStackInSlot(index, prevStacksHandler.getStacks().getStackInSlot(index));
                    newStacksHandler.getCosmeticStacks().setStackInSlot(index,
                        prevStacksHandler.getCosmeticStacks().getStackInSlot(index));
                    index++;
                  }

                  while (index < prevStacksHandler.getSlots()) {
                    instance.loseInvalidStack(prevStacksHandler.getStacks().getStackInSlot(index));
                    instance.loseInvalidStack(
                        prevStacksHandler.getCosmeticStacks().getStackInSlot(index));
                    index++;
                  }
                  sortedCurios.put(type, newStacksHandler);

                  for (int j = 0;
                      j < newStacksHandler.getRenders().size() && j < prevStacksHandler.getRenders()
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
                      instance.loseInvalidStack(stack);
                    }

                    ItemStack cosmeticStack = cosmeticStackHandler.getStackInSlot(j);

                    if (!cosmeticStack.isEmpty()) {
                      instance.loseInvalidStack(cosmeticStack);
                    }
                  }
                }
              }
              sortedCurios.forEach(
                  (slotType, stacksHandler) -> curios.put(slotType.getIdentifier(), stacksHandler));
              instance.setCurios(curios);

              for (int k = 0; k < lockedList.size(); k++) {
                instance.lockSlotType(lockedList.getString(k));
              }
            }
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

    CurioInventoryWrapper() {
      this(null);
    }

    CurioInventoryWrapper(final PlayerEntity playerEntity) {
      this.wearer = playerEntity;
      this.reset();
    }

    @Override
    public void reset() {

      if (!this.wearer.getEntityWorld().isRemote() && this.wearer instanceof ServerPlayerEntity) {
        this.locked.clear();
        this.curios.clear();
        this.invalidStacks.clear();
        CuriosApi.getSlotHelper().createSlots().forEach(
            ((slotType, stacksHandler) -> this.curios.put(slotType.getIdentifier(), stacksHandler)));
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
            new CurioStacksHandler(state.amount, 0, state.visible, state.cosmetic));
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
        this.getStacksHandler(identifier).ifPresent(stackHandler -> {
          int toShrink = Math.min(stackHandler.getSlots() - 1, amount);
          this.loseStacks(stackHandler.getStacks(), identifier, toShrink);
          stackHandler.shrink(amount);
        });
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

          if (!stack.isEmpty()) {
            this.wearer.getAttributeManager().removeModifiers(
                CuriosApi.getCuriosHelper().getAttributeModifiers(identifier, stack));
            int index = i;
            CuriosApi.getCuriosHelper().getCurio(stack)
                .ifPresent(curio -> curio.onUnequip(identifier, index, this.wearer));
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
	public void setEnchantmentBonuses(Tuple<Integer, Integer> fortuneAndLootingIn) {
		this.fortuneAndLooting = fortuneAndLootingIn;
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
