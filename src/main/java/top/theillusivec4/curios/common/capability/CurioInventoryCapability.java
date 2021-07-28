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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemHandlerHelper;
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
    CapabilityManager.INSTANCE.register(ICuriosItemHandler.class);
  }

  public static ICapabilityProvider createProvider(final Player playerEntity) {
    return new Provider(playerEntity);
  }

  public static class CurioInventoryWrapper implements ICuriosItemHandler {
    Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
    NonNullList<ItemStack> invalidStacks = NonNullList.create();
    Player wearer;

    public CurioInventoryWrapper(final Player playerEntity) {
      this.wearer = playerEntity;
      this.reset();
    }

    @Override
    public void reset() {

      if (!this.wearer.getCommandSenderWorld().isClientSide() &&
          this.wearer instanceof ServerPlayer) {
        this.curios.clear();
        this.invalidStacks.clear();
        CuriosApi.getSlotHelper().createSlots().forEach(
            ((slotType, stacksHandler) -> this.curios
                .put(slotType.getIdentifier(), stacksHandler)));
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
        this.getStacksHandler(identifier).ifPresent(stackHandler -> {
          int toShrink = Math.min(stackHandler.getSlots() - 1, amount);
          this.loseStacks(stackHandler.getStacks(), identifier, toShrink, stackHandler);
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

    @Override
    public int getFortuneLevel(@Nullable LootContext lootContext) {
      int fortuneLevel = 0;
      for (Map.Entry<String, ICurioStacksHandler> entry : getCurios().entrySet()) {
        IDynamicStackHandler stacks = entry.getValue().getStacks();

        for (int i = 0; i < stacks.getSlots(); i++) {
          final int index = i;
          fortuneLevel += CuriosApi.getCuriosHelper().getCurio(stacks.getStackInSlot(i)).map(
              curio -> curio.getFortuneLevel(
                  new SlotContext(entry.getKey(), this.wearer, index, false,
                      entry.getValue().getRenders().get(index)), lootContext)).orElse(0);
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
          lootingLevel += CuriosApi.getCuriosHelper().getCurio(stacks.getStackInSlot(i)).map(
              curio -> curio.getLootingLevel(
                  new SlotContext(entry.getKey(), this.wearer, index, false,
                      entry.getValue().getRenders().get(index)), source, target, baseLooting))
              .orElse(0);
        }
      }
      return lootingLevel;
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
      ListTag tagList = ((CompoundTag) nbt).getList("Curios", NBT.TAG_COMPOUND);
      ISlotHelper slotHelper = CuriosApi.getSlotHelper();
      ICuriosHelper curiosHelper = CuriosApi.getCuriosHelper();
      LivingEntity livingEntity = this.getWearer();

      if (!tagList.isEmpty() && slotHelper != null) {
        Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
        SortedMap<ISlotType, ICurioStacksHandler> sortedCurios = slotHelper.createSlots();

        for (int i = 0; i < tagList.size(); i++) {
          CompoundTag tag = tagList.getCompound(i);
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
              ItemStack prevStack = prevStacksHandler.getStacks().getStackInSlot(index);
              SlotContext slotContext = new SlotContext(identifier, livingEntity, index, false,
                  newStacksHandler.getRenders().get(index));

              if (!prevStack.isEmpty()) {

                if (curiosHelper.isStackValid(slotContext, prevStack)) {
                  newStacksHandler.getStacks().setStackInSlot(index, prevStack);
                } else {
                  this.loseInvalidStack(prevStack);
                }
              }
              ItemStack prevCosmetic =
                  prevStacksHandler.getCosmeticStacks().getStackInSlot(index);
              slotContext = new SlotContext(identifier, livingEntity, index, true, true);

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

    private void loseStacks(IDynamicStackHandler stackHandler, String identifier, int amount,
                            ICurioStacksHandler curioStacks) {

      if (this.wearer != null && !this.wearer.getCommandSenderWorld().isClientSide()) {
        List<ItemStack> drops = new ArrayList<>();

        for (int i = stackHandler.getSlots() - amount; i < stackHandler.getSlots(); i++) {
          ItemStack stack = stackHandler.getStackInSlot(i);
          drops.add(stackHandler.getStackInSlot(i));
          SlotContext slotContext =
              new SlotContext(identifier, this.wearer, i, false, curioStacks.getRenders().get(i));

          if (!stack.isEmpty()) {
            UUID uuid = UUID.nameUUIDFromBytes((identifier + i).getBytes());
            this.wearer.getAttributes().removeAttributeModifiers(
                CuriosApi.getCuriosHelper().getAttributeModifiers(slotContext, uuid, stack));
            CuriosApi.getCuriosHelper().getCurio(stack)
                .ifPresent(curio -> curio.onUnequip(slotContext, stack));
          }
          stackHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
        drops.forEach(drop -> ItemHandlerHelper.giveItemToPlayer(this.wearer, drop));
      }
    }
  }

  public static class Provider implements ICapabilitySerializable<Tag> {

    final LazyOptional<ICuriosItemHandler> optional;
    final ICuriosItemHandler handler;

    Provider(final Player playerEntity) {
      this.handler = new CurioInventoryWrapper(playerEntity);
      this.optional = LazyOptional.of(() -> this.handler);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nullable Capability<T> capability, Direction facing) {
      return CuriosCapability.INVENTORY.orEmpty(capability, this.optional);
    }

    @Override
    public Tag serializeNBT() {
      return handler.writeTag();
    }

    @Override
    public void deserializeNBT(Tag nbt) {
      handler.readTag(nbt);
    }
  }
}
