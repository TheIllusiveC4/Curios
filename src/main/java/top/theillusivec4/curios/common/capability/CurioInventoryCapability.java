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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioEquipEvent;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.api.type.util.ICuriosHelper;
import top.theillusivec4.curios.api.type.util.ISlotHelper;
import top.theillusivec4.curios.common.CuriosHelper;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;

public class CurioInventoryCapability {

  public static ICapabilityProvider createProvider(final Player playerEntity) {
    return new Provider(playerEntity);
  }

  public static class CurioInventoryWrapper implements ICuriosItemHandler {
    Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
    NonNullList<ItemStack> invalidStacks = NonNullList.create();
    Player wearer;
    Set<ICurioStacksHandler> updates = new HashSet<>();

    public CurioInventoryWrapper(final Player playerEntity) {
      this.wearer = playerEntity;
      this.reset();
    }

    @Override
    public void reset() {
      ISlotHelper slotHelper = CuriosApi.getSlotHelper();

      if (slotHelper != null && this.wearer != null &&
          !this.wearer.getCommandSenderWorld().isClientSide()) {
        this.curios.clear();
        this.invalidStacks.clear();
        SortedSet<ISlotType> sorted = new TreeSet<>(slotHelper.getSlotTypes(this.wearer));

        for (ISlotType slotType : sorted) {
          this.curios.put(slotType.getIdentifier(),
              new CurioStacksHandler(this, slotType.getIdentifier(), slotType.getSize(),
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

    @Override
    public int getFortuneLevel(@Nullable LootContext lootContext) {
      int fortuneLevel = 0;
      for (Map.Entry<String, ICurioStacksHandler> entry : getCurios().entrySet()) {
        IDynamicStackHandler stacks = entry.getValue().getStacks();

        for (int i = 0; i < stacks.getSlots(); i++) {
          final int index = i;
          fortuneLevel += CuriosApi.getCuriosHelper().getCurio(stacks.getStackInSlot(i)).map(
              curio -> {
                NonNullList<Boolean> renderStates = entry.getValue().getRenders();
                return curio.getFortuneLevel(
                    new SlotContext(entry.getKey(), this.wearer, index, false,
                        renderStates.size() > index && renderStates.get(index)), lootContext);
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
          lootingLevel += CuriosApi.getCuriosHelper().getCurio(stacks.getStackInSlot(i)).map(
                  curio -> {
                    NonNullList<Boolean> renderStates = entry.getValue().getRenders();
                    return curio.getLootingLevel(
                        new SlotContext(entry.getKey(), this.wearer, index, false,
                            renderStates.size() > index && renderStates.get(index)), source, target,
                        baseLooting);
                  })
              .orElse(0);
        }
      }
      return lootingLevel;
    }

    @Override
    public ListTag saveInventory(boolean clear) {
      ListTag taglist = new ListTag();

      for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
        CompoundTag tag = new CompoundTag();
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
    public void loadInventory(ListTag data) {

      if (data != null) {

        for (int i = 0; i < data.size(); i++) {
          CompoundTag tag = data.getCompound(i);
          String identifier = tag.getString("Identifier");
          ICurioStacksHandler stacksHandler = curios.get(identifier);

          if (stacksHandler != null) {
            CompoundTag stacksData = tag.getCompound("Stacks");
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
                  renderStates.size() > i && renderStates.get(i));
              UUID uuid = UUID.nameUUIDFromBytes((id + i).getBytes());
              Multimap<Attribute, AttributeModifier> map =
                  CuriosApi.getCuriosHelper().getAttributeModifiers(slotContext, uuid, stack);

              for (Attribute attribute : map.keySet()) {

                if (attribute instanceof CuriosHelper.SlotAttributeWrapper wrapper) {
                  slots.putAll(wrapper.identifier, map.get(attribute));
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
              new CurioStacksHandler(this, slotType.getIdentifier(), slotType.getSize(),
                  slotType.isVisible(), slotType.hasCosmetic()));
        }

        for (int i = 0; i < tagList.size(); i++) {
          CompoundTag tag = tagList.getCompound(i);
          String identifier = tag.getString("Identifier");
          CurioStacksHandler prevStacksHandler = new CurioStacksHandler(this, identifier);
          prevStacksHandler.deserializeNBT(tag.getCompound("StacksHandler"));

          Optional<ISlotType> optionalType = CuriosApi.getSlotHelper()
              .getSlotType(identifier);
          optionalType.ifPresent(type -> {
            CurioStacksHandler newStacksHandler =
                new CurioStacksHandler(this, type.getIdentifier(), type.getSize(), type.isVisible(),
                    type.hasCosmetic());
            newStacksHandler.copyModifiers(prevStacksHandler);
            int index = 0;

            while (index < newStacksHandler.getSlots() && index < prevStacksHandler
                .getSlots()) {
              ItemStack prevStack = prevStacksHandler.getStacks().getStackInSlot(index);
              NonNullList<Boolean> renderStates = newStacksHandler.getRenders();
              SlotContext slotContext = new SlotContext(identifier, livingEntity, index, false,
                  renderStates.size() > index && renderStates.get(index));

              if (!prevStack.isEmpty()) {

                if (this.canEquip(slotContext, prevStack)) {
                  newStacksHandler.getStacks().setStackInSlot(index, prevStack);
                } else {
                  this.loseInvalidStack(prevStack);
                }
              }
              ItemStack prevCosmetic =
                  prevStacksHandler.getCosmeticStacks().getStackInSlot(index);
              slotContext = new SlotContext(identifier, livingEntity, index, true, true);

              if (!prevCosmetic.isEmpty()) {

                if (this.canEquip(slotContext, prevCosmetic)) {
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

    private boolean canEquip(SlotContext slotContext, ItemStack stack) {
      CurioEquipEvent equipEvent = new CurioEquipEvent(stack, slotContext);
      MinecraftForge.EVENT_BUS.post(equipEvent);
      Event.Result result = equipEvent.getResult();

      if (result == Event.Result.DENY) {
        return false;
      }
      return result == Event.Result.ALLOW ||
          (CuriosApi.getCuriosHelper().isStackValid(slotContext, stack) &&
              CuriosApi.getCuriosHelper().getCurio(stack).map(curio -> curio.canEquip(slotContext))
                  .orElse(true));
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
