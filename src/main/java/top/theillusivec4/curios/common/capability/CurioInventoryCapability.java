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
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.inventory.CurioStacksHandler;
import top.theillusivec4.curios.api.type.ICurioItemHandler;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncOperation;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncOperation.Operation;

public class CurioInventoryCapability {

  public static void register() {
    CapabilityManager.INSTANCE
        .register(ICurioItemHandler.class, new Capability.IStorage<ICurioItemHandler>() {
          @Override
          public INBT writeNBT(Capability<ICurioItemHandler> capability, ICurioItemHandler instance,
              Direction side) {
            CompoundNBT compound = new CompoundNBT();
            ListNBT taglist = new ListNBT();
            instance.getCurios().forEach((key, stacksHandler) -> {
              CompoundNBT tag = new CompoundNBT();
              tag.put("StacksHandler", stacksHandler.serializeNBT());
              tag.putString("Identifier", key);
              taglist.add(tag);
            });
            compound.put("Curios", taglist);
            return compound;
          }

          @Override
          public void readNBT(Capability<ICurioItemHandler> capability, ICurioItemHandler instance,
              Direction side, INBT nbt) {
            ListNBT tagList = ((CompoundNBT) nbt).getList("Curios", Constants.NBT.TAG_COMPOUND);

            if (!tagList.isEmpty()) {
              instance.reset();
              Map<String, CurioStacksHandler> curios = instance.getCurios();

              for (int i = 0; i < tagList.size(); i++) {
                CompoundNBT tag = tagList.getCompound(i);
                String identifier = tag.getString("Identifier");
                CurioStacksHandler prevStacksHandler = new CurioStacksHandler();
                prevStacksHandler.deserializeNBT(tag.getCompound("StacksHandler"));

                Optional<ISlotType> optionalType = CuriosApi.getType(identifier);
                optionalType.ifPresent(type -> {
                  int targetSize = type.getSize() + prevStacksHandler.getSizeShift();
                  CurioStacksHandler newStacksHandler = new CurioStacksHandler(targetSize);
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
                  curios.put(identifier, newStacksHandler);
                });

                if (!optionalType.isPresent()) {
                  ItemStackHandler stackHandler = prevStacksHandler.getStacks();
                  ItemStackHandler cosmeticStackHandler = prevStacksHandler.getCosmeticStacks();

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
              instance.setCurios(curios);
            }
          }
        }, CurioInventoryWrapper::new);
  }

  public static ICapabilityProvider createProvider(final PlayerEntity playerEntity) {
    return new Provider(playerEntity);
  }

  public static class CurioInventoryWrapper implements ICurioItemHandler {

    Map<String, CurioStacksHandler> curios = new LinkedHashMap<>();
    SortedSet<ISlotType> sortedTypes = new TreeSet<>();
    NonNullList<ItemStack> invalidStacks = NonNullList.create();
    PlayerEntity wearer;

    CurioInventoryWrapper() {
      this(null);
    }

    CurioInventoryWrapper(final PlayerEntity playerEntity) {
      this.wearer = playerEntity;
      this.reset();
    }

    @Override
    public void reset() {
      Set<ISlotType> types = CuriosApi.getTypes().stream().filter(type -> !type.isLocked())
          .collect(Collectors.toSet());
      this.sortedTypes.addAll(types);
      this.sortedTypes.forEach(
          type -> curios.put(type.getIdentifier(), new CurioStacksHandler(type.getSize())));
    }

    @Override
    public int getSlots() {
      return this.curios.values().stream().mapToInt(CurioStacksHandler::getSlots).sum();
    }

    @Override
    public Optional<CurioStacksHandler> getStacksHandler(String identifier) {
      return Optional.ofNullable(this.curios.get(identifier));
    }

    @Override
    public Map<String, CurioStacksHandler> getCurios() {
      return Collections.unmodifiableMap(this.curios);
    }

    @Override
    public void setCurios(Map<String, CurioStacksHandler> curios) {
      this.curios = curios;
      this.sortedTypes.clear();
      curios.keySet()
          .forEach(id -> CuriosApi.getType(id).ifPresent(type -> this.sortedTypes.add(type)));
    }

    @Override
    public void unlockSlotType(String identifier) {
      CuriosApi.getType(identifier).ifPresent(type -> {
        this.curios.putIfAbsent(identifier, new CurioStacksHandler(type.getSize()));
        this.sortedTypes.add(type);

        if (!wearer.world.isRemote && wearer instanceof ServerPlayerEntity) {
          NetworkHandler.INSTANCE
              .send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) wearer),
                  new SPacketSyncOperation(wearer.getEntityId(), identifier, Operation.UNLOCK));
        }
      });
    }

    @Override
    public void lockSlotType(String identifier) {
      this.getStacksHandler(identifier).ifPresent(stackHandler -> {
        this.loseStacks(stackHandler.getStacks(), identifier, stackHandler.getSlots());
        this.curios.remove(identifier);
        CuriosApi.getType(identifier).ifPresent(type -> this.sortedTypes.remove(type));

        if (wearer instanceof ServerPlayerEntity) {
          NetworkHandler.INSTANCE
              .send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) wearer),
                  new SPacketSyncOperation(wearer.getEntityId(), identifier, Operation.LOCK));
        }
      });
    }

    @Override
    public void growSlotType(String identifier, int amount) {

      if (amount > 0) {
        this.getStacksHandler(identifier).ifPresent(stackHandler -> {
          stackHandler.grow(amount);

          if (wearer instanceof ServerPlayerEntity) {
            NetworkHandler.INSTANCE.sendTo(
                new SPacketSyncOperation(wearer.getEntityId(), identifier, Operation.GROW, amount),
                ((ServerPlayerEntity) wearer).connection.getNetworkManager(),
                NetworkDirection.PLAY_TO_CLIENT);
          }
        });
      }
    }

    @Override
    public void shrinkSlotType(String identifier, int amount) {

      if (amount > 0) {
        this.getStacksHandler(identifier).ifPresent(stackHandler -> {
          int toShrink = Math.min(stackHandler.getSlots() - 1, amount);
          this.loseStacks(stackHandler.getStacks(), identifier, toShrink);

          if (wearer instanceof ServerPlayerEntity) {
            NetworkHandler.INSTANCE.sendTo(
                new SPacketSyncOperation(wearer.getEntityId(), identifier, Operation.SHRINK,
                    amount), ((ServerPlayerEntity) wearer).connection.getNetworkManager(),
                NetworkDirection.PLAY_TO_CLIENT);
          }
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
        this.invalidStacks.forEach(drop -> ItemHandlerHelper.giveItemToPlayer(wearer, drop));
        this.invalidStacks = NonNullList.create();
      }
    }

    private void loseStacks(ItemStackHandler stackHandler, String identifier, int amount) {

      if (this.wearer != null && !this.wearer.getEntityWorld().isRemote()) {
        List<ItemStack> drops = new ArrayList<>();

        for (int i = stackHandler.getSlots() - amount; i < stackHandler.getSlots(); i++) {
          ItemStack stack = stackHandler.getStackInSlot(i);
          drops.add(stackHandler.getStackInSlot(i));

          if (!stack.isEmpty()) {
            wearer.getAttributes()
                .removeAttributeModifiers(CuriosApi.getAttributeModifiers(identifier, stack));
            int index = i;
            CuriosApi.getCurio(stack)
                .ifPresent(curio -> curio.onUnequip(identifier, index, this.wearer));
          }
          stackHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
        drops.forEach(drop -> ItemHandlerHelper.giveItemToPlayer(wearer, drop));
      }
    }
  }

  public static class Provider implements ICapabilitySerializable<INBT> {

    final LazyOptional<ICurioItemHandler> optional;
    final ICurioItemHandler handler;

    Provider(final PlayerEntity playerEntity) {
      this.handler = new CurioInventoryWrapper(playerEntity);
      this.optional = LazyOptional.of(() -> handler);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nullable Capability<T> capability, Direction facing) {
      return CuriosCapability.INVENTORY.orEmpty(capability, optional);
    }

    @Override
    public INBT serializeNBT() {
      return CuriosCapability.INVENTORY.writeNBT(handler, null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
      CuriosCapability.INVENTORY.readNBT(handler, null, nbt);
    }
  }
}
