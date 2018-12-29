package c4.curios.api.capability;

import c4.curios.Curios;
import c4.curios.api.CuriosAPI;
import c4.curios.api.inventory.CurioSlotEntry;
import c4.curios.api.inventory.CurioStackHandler;
import com.google.common.collect.Maps;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

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
                Map<String, CurioStackHandler> curioMap = instance.getCurioMap();
                NBTTagCompound compound = new NBTTagCompound();
                NBTTagList taglist = new NBTTagList();

                for (String identifier : curioMap.keySet()) {
                    NBTTagCompound itemtag = new NBTTagCompound();
                    itemtag.setString("Identifier", identifier);
                    CurioStackHandler stackHandler = curioMap.get(identifier);
                    ItemStackHelper.saveAllItems(itemtag, stackHandler.getStacks());
                    taglist.appendTag(itemtag);
                }
                compound.setTag("Curios", taglist);
                return compound;
            }

            @Override
            public void readNBT(Capability<ICurioItemHandler> capability, ICurioItemHandler instance, EnumFacing side, NBTBase nbt) {
                NBTTagList tagList = ((NBTTagCompound)nbt).getTagList("Curios", Constants.NBT.TAG_COMPOUND);

                if (!tagList.isEmpty()) {
                    for (int i = 0; i < tagList.tagCount(); i++) {
                        NBTTagCompound itemtag = tagList.getCompoundTagAt(i);
                        String identifier = itemtag.getString("Identifier");
                        CurioSlotEntry entry = CuriosAPI.getSlotEntryForID(identifier);

                        if (entry != null) {
                            NonNullList<ItemStack> stacks = NonNullList.withSize(instance.getStackHandler(identifier).getSlots(),
                                    ItemStack.EMPTY);
                            ItemStackHelper.loadAllItems(itemtag, stacks);
                            instance.getCurioMap().put(identifier, new CurioStackHandler(entry, stacks));
                        }
                    }
                }
            }
        }, CurioInventoryWrapper::new);
    }

    public static ICapabilityProvider createProvider(final ICurioItemHandler curios) {
        return new Provider(curios, CURIO_INV_CAP, DEFAULT_FACING);
    }

    public static class CurioInventoryWrapper implements ICurioItemHandler {

        Map<String, CurioStackHandler> curioSlots;
        Map<String, CurioStackHandler> prevCurioSlots;

        public CurioInventoryWrapper() {
            this.curioSlots = Maps.newLinkedHashMap();
            this.prevCurioSlots = Maps.newLinkedHashMap();
        }

        public CurioInventoryWrapper(EntityLivingBase livingBase) {
            this.curioSlots = CuriosAPI.getSlotsMap(livingBase);
            this.prevCurioSlots = CuriosAPI.getSlotsMap(livingBase);
        }

        @Override
        public void setStackInSlot(String identifier, int slot, @Nonnull ItemStack stack) {
            this.curioSlots.get(identifier).setStackInSlot(slot, stack);
        }

        @Override
        public int getSlots() {
            int totalSlots = 0;

            for (CurioStackHandler stacks : curioSlots.values()) {
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
        public Map<String, CurioStackHandler> getCurioMap() {
            return this.curioSlots;
        }

        @Override
        public void setCurioMap(Map<String, CurioStackHandler> map) {
            this.curioSlots = map;
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
