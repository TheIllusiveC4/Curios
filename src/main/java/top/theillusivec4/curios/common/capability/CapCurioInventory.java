package top.theillusivec4.curios.common.capability;

import com.google.common.collect.Maps;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
import top.theillusivec4.curios.api.CuriosHelper;
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
                return compound;
            }

            @Override
            public void readNBT(Capability<ICurioItemHandler> capability, ICurioItemHandler instance, EnumFacing side, INBTBase nbt) {
                NBTTagList tagList = ((NBTTagCompound)nbt).getList("Curios", Constants.NBT.TAG_COMPOUND);
                SortedMap<String, CurioStackHandler> curios = Maps.newTreeMap();

                if (!tagList.isEmpty()) {

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
        EntityLivingBase wearer;

        CurioInventoryWrapper() {
            this(null);
        }

        CurioInventoryWrapper(final EntityLivingBase livingBase) {
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
                CurioType type = CuriosRegistry.getType(id);

                if (type != null && type.isEnabled()) {
                    slots.put(id, new CurioStackHandler(type.getSize()));
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
                    CuriosHelper.getCurio(stack).ifPresent(curio -> {
                        if (!stack.isEmpty()) {
                            curio.onUnequipped(stack, identifier, wearer);
                            wearer.getAttributeMap().removeAttributeModifiers(curio.getAttributeModifiers(identifier, stack));
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
