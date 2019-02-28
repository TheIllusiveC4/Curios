/*
 * Copyright (C) 2018-2019  C4
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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.CurioType;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.CuriosRegistry;
import top.theillusivec4.curios.api.capability.CuriosCapability;
import top.theillusivec4.curios.api.capability.ICurioItemHandler;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncActive;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncSize;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;

public class CapCurioInventory {

    public static void register() {
        CapabilityManager.INSTANCE.register(ICurioItemHandler.class, new Capability.IStorage<ICurioItemHandler>() {
            @Override
            public INBTBase writeNBT(Capability<ICurioItemHandler> capability, ICurioItemHandler instance,
                                     EnumFacing side) {
                SortedMap<String, CurioStackHandler> curioMap = instance.getCurioMap();
                NBTTagCompound compound = new NBTTagCompound();
                NBTTagList taglist = new NBTTagList();

                for (String identifier : curioMap.keySet()) {
                    CurioStackHandler stackHandler = curioMap.get(identifier);
                    NBTTagCompound itemtag = stackHandler.serializeNBT();
                    itemtag.setString("Identifier", identifier);
                    taglist.add(itemtag);
                }
                compound.setTag("Curios", taglist);

                NBTTagList taglist1 = new NBTTagList();

                for (String identifier : instance.getDisabled()) {
                    taglist1.add(new NBTTagString(identifier));
                }
                compound.setTag("Disabled", taglist1);
                return compound;
            }

            @Override
            public void readNBT(Capability<ICurioItemHandler> capability, ICurioItemHandler instance, EnumFacing side, INBTBase nbt) {
                NBTTagList tagList = ((NBTTagCompound)nbt).getList("Curios", Constants.NBT.TAG_COMPOUND);
                NBTTagList tagList1 = ((NBTTagCompound)nbt).getList("Disabled", Constants.NBT.TAG_STRING);
                Set<String> disabled = Sets.newHashSet();

                for (int k = 0; k < tagList1.size(); k++) {
                    disabled.add(tagList1.getString(k));
                }
                instance.setDisabled(disabled);

                if (!tagList.isEmpty()) {
                    SortedMap<String, CurioStackHandler> curios = instance.getDefaultSlots();

                    for (int i = 0; i < tagList.size(); i++) {
                        NBTTagCompound itemtag = tagList.getCompound(i);
                        String identifier = itemtag.getString("Identifier");
                        CurioType type = CuriosRegistry.getType(identifier);
                        CurioStackHandler stackHandler = new CurioStackHandler();
                        stackHandler.deserializeNBT(itemtag);

                        if (type != null) {
                            curios.put(identifier, stackHandler);
                        } else {

                            for (int j = 0; j < stackHandler.getSlots(); j++) {
                                ItemStack stack = stackHandler.getStackInSlot(j);

                                if (!stack.isEmpty()) {
                                    instance.addInvalid(stackHandler.getStackInSlot(j));
                                }
                            }
                        }
                    }
                    instance.setCurioMap(curios);
                }
            }
        }, CurioInventoryWrapper::new);
    }

    public static ICapabilityProvider createProvider(final EntityLivingBase livingBase) {
        return new Provider(livingBase);
    }

    public static class CurioInventoryWrapper implements ICurioItemHandler {

        SortedMap<String, CurioStackHandler> curioSlots;
        NonNullList<ItemStack> invalidCache;
        Set<String> disabled;
        EntityLivingBase wearer;

        CurioInventoryWrapper() {
            this(null);
        }

        CurioInventoryWrapper(final EntityLivingBase livingBase) {
            this.disabled = Sets.newHashSet();
            this.curioSlots = this.getDefaultSlots();
            this.invalidCache = NonNullList.create();
            this.wearer = livingBase;
        }

        @Override
        public void setStackInSlot(String identifier, int slot, @Nonnull ItemStack stack) {
            this.curioSlots.get(identifier).setStackInSlot(slot, stack);
        }

        @Override
        public SortedMap<String, CurioStackHandler> getDefaultSlots() {
            SortedMap<String, CurioStackHandler> slots = Maps.newTreeMap();

            for (String id : CuriosRegistry.getTypeIdentifiers()) {

                if (disabled.isEmpty() || !disabled.contains(id)) {
                    CurioType type = CuriosRegistry.getType(id);

                    if (type != null && type.isEnabled()) {
                        CurioStackHandler handler = new CurioStackHandler(type.getSize());
                        handler.setHidden(type.isHidden());
                        handler.setIcon(type.getIcon() == null ? "" : type.getIcon().toString());
                        slots.put(id, handler);
                    }
                }
            }
            return slots;
        }

        @Override
        public int getSlots() {
            int totalSlots = 0;

            for (ItemStackHandler stacks : curioSlots.values()) {
                totalSlots += stacks.getSlots();
            }
            return totalSlots;
        }

        @Nonnull
        public ItemStack getStackInSlot(String identifier, int slot) {
            return this.curioSlots.get(identifier).getStackInSlot(slot);
        }

        @Nullable
        @Override
        public CurioStackHandler getStackHandler(String identifier) {
            return this.curioSlots.get(identifier);
        }

        @Override
        public SortedMap<String, CurioStackHandler> getCurioMap() {
            return Collections.unmodifiableSortedMap(this.curioSlots);
        }

        @Override
        public void setCurioMap(SortedMap<String, CurioStackHandler> map) {
            this.curioSlots = map;
        }

        @Override
        public void enableCurio(String identifier) {
            CurioType type = CuriosRegistry.getType(identifier);

            if (type != null) {
                this.curioSlots.putIfAbsent(identifier, new CurioStackHandler(type.getSize()));
                this.disabled.remove(identifier);

                if (!wearer.world.isRemote && wearer instanceof EntityPlayerMP) {
                    NetworkHandler.INSTANCE.sendTo(new SPacketSyncActive(wearer.getEntityId(), identifier, false),
                            ((EntityPlayerMP)wearer).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
                }
            }
        }

        @Override
        public void disableCurio(String identifier) {
            CurioStackHandler stackHandler = this.curioSlots.get(identifier);

            if (stackHandler != null) {
                dropOrGiveLast(stackHandler, identifier, stackHandler.getSlots());
                this.curioSlots.remove(identifier);
                this.disabled.add(identifier);

                if (wearer instanceof EntityPlayerMP) {
                    NetworkHandler.INSTANCE.sendTo(new SPacketSyncActive(wearer.getEntityId(), identifier, true),
                            ((EntityPlayerMP) wearer).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
                }
            }
        }

        @Override
        public void addCurioSlot(String identifier, int amount) {

            if (amount > 0) {
                CurioStackHandler stackHandler = this.curioSlots.get(identifier);

                if (stackHandler != null) {
                    stackHandler.addSize(amount);

                    if (wearer instanceof EntityPlayerMP) {
                        NetworkHandler.INSTANCE.sendTo(new SPacketSyncSize(wearer.getEntityId(), identifier, amount, false),
                                ((EntityPlayerMP) wearer).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
                    }
                }
            }
        }

        @Override
        public void removeCurioSlot(String identifier, int amount) {

            if (amount > 0) {
                CurioStackHandler stackHandler = this.curioSlots.get(identifier);

                if (stackHandler != null) {
                    amount = Math.min(stackHandler.getSlots() - 1, amount);
                    dropOrGiveLast(stackHandler, identifier, amount);

                    if (wearer instanceof EntityPlayerMP) {
                        NetworkHandler.INSTANCE.sendTo(new SPacketSyncSize(wearer.getEntityId(), identifier, amount, true),
                                ((EntityPlayerMP) wearer).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
                    }
                    stackHandler.removeSize(amount);
                }
            }
        }

        @Nullable
        @Override
        public EntityLivingBase getWearer() {
            return this.wearer;
        }

        @Override
        public ImmutableSet<String> getDisabled() {
            return ImmutableSet.copyOf(disabled);
        }

        @Override
        public void setDisabled(Set<String> disabled) {
            this.disabled = disabled;
        }

        @Override
        public void addInvalid(ItemStack stack) {
            this.invalidCache.add(stack);
        }

        @Override
        public void dropInvalidCache() {

            if (!this.invalidCache.isEmpty()) {
                dropOrGive(this.invalidCache);
                this.invalidCache = NonNullList.create();
            }
        }

        private void dropOrGiveLast(ItemStackHandler stackHandler, String identifier, int amount) {

            if (!wearer.world.isRemote) {
                NonNullList<ItemStack> drops = NonNullList.create();

                for (int i = stackHandler.getSlots() - amount; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);
                    drops.add(stackHandler.getStackInSlot(i));
                    CuriosAPI.getCurio(stack).ifPresent(curio -> {
                        if (!stack.isEmpty()) {
                            wearer.getAttributeMap().removeAttributeModifiers(curio.getAttributeModifiers(identifier));
                        }
                    });
                    stackHandler.setStackInSlot(i, ItemStack.EMPTY);
                }
                dropOrGive(drops);
            }
        }

        private void dropOrGive(NonNullList<ItemStack> drops) {

            if (wearer instanceof EntityPlayer) {

                for (ItemStack drop : drops) {
                    ItemHandlerHelper.giveItemToPlayer((EntityPlayer) wearer, drop);
                }
            } else {

                for (ItemStack drop : drops) {
                    wearer.entityDropItem(drop, 0.0f);
                }
            }
        }
    }

    public static class Provider implements ICapabilitySerializable<INBTBase> {

        final LazyOptional<ICurioItemHandler> optional;
        final ICurioItemHandler handler;

        Provider(final EntityLivingBase livingBase) {
            this.handler = new CurioInventoryWrapper(livingBase);
            this.optional = LazyOptional.of(() -> handler);
        }

        @SuppressWarnings("ConstantConditions")
        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nullable Capability<T> capability, EnumFacing facing) {
            return CuriosCapability.INVENTORY.orEmpty(capability, optional);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public INBTBase serializeNBT() {
            return CuriosCapability.INVENTORY.writeNBT(handler, null);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void deserializeNBT(INBTBase nbt) {
            CuriosCapability.INVENTORY.readNBT(handler, null, nbt);
        }
    }
}
