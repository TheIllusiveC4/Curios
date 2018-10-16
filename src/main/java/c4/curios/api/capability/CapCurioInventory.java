package c4.curios.api.capability;

import c4.curios.Curios;
import c4.curios.api.inventory.CurioSlot;
import c4.curios.api.inventory.CurioSlotInfo;
import c4.curios.api.CuriosAPI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapCurioInventory {

    @CapabilityInject(ICurioItemHandler.class)
    public static final Capability<ICurioItemHandler> CURIO_INV_CAP = null;

    public static final EnumFacing DEFAULT_FACING = null;
    public static final ResourceLocation ID = new ResourceLocation(Curios.MODID, "curios_inventory");

    public static void register() {
        CapabilityManager.INSTANCE.register(ICurioItemHandler.class, new Capability.IStorage<ICurioItemHandler>() {
            @Override
            public NBTBase writeNBT(Capability<ICurioItemHandler> capability, ICurioItemHandler instance,
                                    EnumFacing side) {
                NBTTagList nbtTagList = new NBTTagList();
                NonNullList<CurioSlot> curioStacks = instance.getCurioStacks();

                for (CurioSlot slot : curioStacks) {
                    NBTTagCompound itemTag = new NBTTagCompound();
                    itemTag.setString("Identifier", slot.getInfo().getIdentifier());
                    slot.getStack().writeToNBT(itemTag);
                    nbtTagList.appendTag(itemTag);
                }
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setTag("Items", nbtTagList);
                return nbt;
            }

            @Override
            public void readNBT(Capability<ICurioItemHandler> capability, ICurioItemHandler instance, EnumFacing side, NBTBase nbt) {
                NBTTagList tagList = ((NBTTagCompound)nbt).getTagList("Items", Constants.NBT.TAG_COMPOUND);
                NonNullList<CurioSlot> curioStacks = NonNullList.create();

                for (int i = 0; i < tagList.tagCount(); i++) {
                    NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
                    CurioSlotInfo info = CuriosAPI.getSlotFromID(itemTags.getString("Identifier"));

                    if (info != null) {
                        curioStacks.add(new CurioSlot(info, new ItemStack(itemTags)));
                    }
                }
                instance.setCurioStacks(curioStacks);
            }
        }, CurioInventoryWrapper::new);
    }

    public static ICapabilityProvider createProvider(final ICurioItemHandler curios) {
        return new Provider(curios, CURIO_INV_CAP, DEFAULT_FACING);
    }

    public static class CurioInventoryWrapper implements ICurioItemHandler {

        NonNullList<CurioSlot> curioStacks;

        public CurioInventoryWrapper() {
            this.curioStacks = NonNullList.create();
        }

        public CurioInventoryWrapper(EntityPlayer player) {
            this.curioStacks = initCurios();
        }

        private NonNullList<CurioSlot> initCurios() {
            NonNullList<CurioSlot> curios = NonNullList.create();
            for (CurioSlotInfo info : CuriosAPI.getSlotList()) {
                for (int i = 0; i < info.getMaxSlots(); i++) {
                    curios.add(new CurioSlot(info));
                }
            }
            return curios;
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            validateSlotIndex(slot);
            this.curioStacks.get(slot).setStack(stack);
            onContentsChanged(slot);
        }

        @Override
        public int getSlots() {
            return curioStacks.size();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return curioStacks.get(slot).getStack();
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

            if (stack.isEmpty()) return ItemStack.EMPTY;

            validateSlotIndex(slot);

            ItemStack existing = this.curioStacks.get(slot).getStack();

            int limit = getStackLimit(slot, stack);

            if (!existing.isEmpty()) {
                if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) return stack;
                limit -= existing.getCount();
            }

            if (limit <= 0) return stack;

            boolean reachedLimit = stack.getCount() > limit;

            if (!simulate) {
                if (existing.isEmpty()) {
                    this.curioStacks.get(slot).setStack(reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) :
                            stack);
                }
                else {
                    existing.grow(reachedLimit ? limit : stack.getCount());
                }
                onContentsChanged(slot);
            }

            return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount()- limit) : ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {

            if (amount == 0) return ItemStack.EMPTY;

            validateSlotIndex(slot);

            ItemStack existing = this.curioStacks.get(slot).getStack();

            if (existing.isEmpty()) return ItemStack.EMPTY;

            int toExtract = Math.min(amount, existing.getMaxStackSize());

            if (existing.getCount() <= toExtract) {
                if (!simulate) {
                    this.curioStacks.get(slot).setStack(ItemStack.EMPTY);
                    onContentsChanged(slot);
                }
                return existing;
            }
            else {
                if (!simulate) {
                    this.curioStacks.get(slot).setStack(ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
                    onContentsChanged(slot);
                }

                return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
            return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        }

        protected void validateSlotIndex(int slot) {
            if (slot < 0 || slot >= curioStacks.size())
                throw new RuntimeException("Slot " + slot + " not in valid range - [0," + curioStacks.size() + ")");
        }

        protected void onContentsChanged(int slot) {}

        @Override
        public NonNullList<CurioSlot> getCurioStacks() {
            return curioStacks;
        }

        @Override
        public void setCurioStacks(NonNullList<CurioSlot> curioStacks) {

            for (int i = 0; i < this.curioStacks.size() && i < curioStacks.size(); i++) {
                this.curioStacks.set(i, curioStacks.get(i));
            }
        }
    }

    public static class Provider implements ICapabilitySerializable<NBTBase> {

        final Capability<ICurioItemHandler> capability;
        final EnumFacing facing;
        final ICurioItemHandler instance;

        Provider(final ICurioItemHandler instance, final Capability<ICurioItemHandler> capability, @Nullable final EnumFacing facing) {
            this.instance = instance;
            this.capability = capability;
            this.facing = facing;
        }

        @Override
        public boolean hasCapability(@Nullable final Capability<?> capability, final EnumFacing facing) {
            return capability == getCapability();
        }

        @Override
        public <T> T getCapability(@Nullable Capability<T> capability, EnumFacing facing) {
            return capability == getCapability() ? getCapability().cast(this.instance) : null;
        }

        final Capability<ICurioItemHandler> getCapability() {
            return capability;
        }

        EnumFacing getFacing() {
            return facing;
        }

        final ICurioItemHandler getInstance() {
            return instance;
        }

        @Override
        public NBTBase serializeNBT() {
            return getCapability().writeNBT(getInstance(), getFacing());
        }

        @Override
        public void deserializeNBT(NBTBase nbt) {
            getCapability().readNBT(getInstance(), getFacing(), nbt);
        }
    }
}
