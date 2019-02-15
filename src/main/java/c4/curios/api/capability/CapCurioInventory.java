package c4.curios.api.capability;

import c4.curios.Curios;
import c4.curios.api.CuriosAPI;
import c4.curios.api.inventory.CurioSlotEntry;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
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
import net.minecraftforge.items.ItemStackHandler;

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
                ImmutableMap<String, ItemStackHandler> curioMap = instance.getCurioMap();
                NBTTagCompound compound = new NBTTagCompound();
                NBTTagList taglist = new NBTTagList();

                for (String identifier : curioMap.keySet()) {
                    ItemStackHandler stackHandler = curioMap.get(identifier);
                    NBTTagCompound itemtag = stackHandler.serializeNBT();
                    itemtag.setString("Identifier", identifier);
                    taglist.appendTag(itemtag);
                }
                compound.setTag("Curios", taglist);
                return compound;
            }

            @Override
            public void readNBT(Capability<ICurioItemHandler> capability, ICurioItemHandler instance, EnumFacing side, NBTBase nbt) {
                NBTTagList tagList = ((NBTTagCompound)nbt).getTagList("Curios", Constants.NBT.TAG_COMPOUND);
                Map<String, ItemStackHandler> curios = Maps.newLinkedHashMap();
                curios.putAll(instance.getCurioMap());

                if (!tagList.isEmpty()) {

                    for (int i = 0; i < tagList.tagCount(); i++) {
                        NBTTagCompound itemtag = tagList.getCompoundTagAt(i);
                        String identifier = itemtag.getString("Identifier");
                        CurioSlotEntry entry = CuriosAPI.getRegistry().get(identifier);

                        if (entry != null) {
                            ItemStackHandler stackHandler = instance.getStackHandler(identifier);

                            if (stackHandler != null) {
                                NonNullList<ItemStack> stacks = NonNullList.withSize(instance.getStackHandler(identifier).getSlots(),
                                        ItemStack.EMPTY);
                                ItemStackHelper.loadAllItems(itemtag, stacks);
                                curios.put(identifier, new ItemStackHandler(stacks));
                            }
                        }
                    }
                    instance.setCurioMap(curios);
                }
            }
        }, CurioInventoryWrapper::new);
    }

    public static ICapabilityProvider createProvider(final ICurioItemHandler curios) {
        return new Provider(curios, CURIO_INV_CAP, DEFAULT_FACING);
    }

    public static class CurioInventoryWrapper implements ICurioItemHandler {

        Map<String, ItemStackHandler> curioSlots;
        Map<String, ItemStackHandler> prevCurioSlots;
        NonNullList<ItemStack> itemCache;

        public CurioInventoryWrapper() {
            this.curioSlots = this.initCurioSlots();
            this.prevCurioSlots = this.initCurioSlots();
            this.itemCache = NonNullList.create();
        }

        @Override
        public void setStackInSlot(String identifier, int slot, @Nonnull ItemStack stack) {
            this.curioSlots.get(identifier).setStackInSlot(slot, stack);
        }

        private Map<String, ItemStackHandler> initCurioSlots() {
            Map<String, ItemStackHandler> slots = Maps.newLinkedHashMap();
            Map<String, CurioSlotEntry> registry = CuriosAPI.getRegistry();

            for (String id : registry.keySet()) {
                CurioSlotEntry entry = registry.get(id);

                if (entry.isEnabled()) {
                    slots.put(id, new ItemStackHandler(entry.getSize()));
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
        public ItemStackHandler getStackHandler(String identifier) {
            return this.curioSlots.get(identifier);
        }

        @Override
        public ImmutableMap<String, ItemStackHandler> getCurioMap() {
            return ImmutableMap.copyOf(this.curioSlots);
        }

        @Override
        public void setCurioMap(Map<String, ItemStackHandler> map) {

            for (String id : map.keySet()) {

                if (!CuriosAPI.getRegistry().containsKey(id)) {
                    map.remove(id);
                }
            }
            this.curioSlots = map;
        }

        @Override
        public ImmutableMap<String, ItemStackHandler> getPreviousCurioMap() {
            return ImmutableMap.copyOf(this.prevCurioSlots);
        }

        @Override
        public void addCurioSlot(String identifier) {
            CurioSlotEntry entry = CuriosAPI.getRegistry().get(identifier);

            if (entry != null) {
                this.curioSlots.putIfAbsent(identifier, new ItemStackHandler(entry.getSize()));
            }
        }

        @Override
        public void removeCurioSlot(String identifier) {
            this.curioSlots.remove(identifier);
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
