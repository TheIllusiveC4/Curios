package top.theillusivec4.curios.api.capability;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.CurioType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class CapCurioInventory {

    @CapabilityInject(ICurioItemHandler.class)
    public static final Capability<ICurioItemHandler> CURIO_INV_CAP = null;

    public static final ResourceLocation ID = new ResourceLocation(Curios.MODID, "curios_inventory");

    public static void register() {
        CapabilityManager.INSTANCE.register(ICurioItemHandler.class, new Capability.IStorage<ICurioItemHandler>() {
            @Override
            public INBTBase writeNBT(Capability<ICurioItemHandler> capability, ICurioItemHandler instance,
                                     EnumFacing side) {
                ImmutableMap<String, ItemStackHandler> curioMap = instance.getCurioMap();
                NBTTagCompound compound = new NBTTagCompound();
                NBTTagList taglist = new NBTTagList();

                for (String identifier : curioMap.keySet()) {
                    ItemStackHandler stackHandler = curioMap.get(identifier);
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
                Map<String, ItemStackHandler> curios = Maps.newLinkedHashMap();
                curios.putAll(instance.getCurioMap());

                if (!tagList.isEmpty()) {

                    for (int i = 0; i < tagList.size(); i++) {
                        NBTTagCompound itemtag = tagList.getCompound(i);
                        String identifier = itemtag.getString("Identifier");
                        CurioType type = CuriosAPI.getType(identifier);

                        if (type != null) {
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

    public static ICapabilityProvider createProvider(final EntityLivingBase livingBase) {
        return new Provider(livingBase);
    }

    public static class CurioInventoryWrapper implements ICurioItemHandler {

        Map<String, ItemStackHandler> curioSlots;
        Map<String, ItemStackHandler> prevCurioSlots;
        NonNullList<ItemStack> itemCache;
        EntityLivingBase wearer;

        public CurioInventoryWrapper() {
            this.curioSlots = this.initCurioSlots();
            this.prevCurioSlots = this.initCurioSlots();
            this.itemCache = NonNullList.create();
        }

        public CurioInventoryWrapper(final EntityLivingBase livingBase) {
            this();
            this.wearer = livingBase;
        }

        @Override
        public void setStackInSlot(String identifier, int slot, @Nonnull ItemStack stack) {
            this.curioSlots.get(identifier).setStackInSlot(slot, stack);
        }

        private Map<String, ItemStackHandler> initCurioSlots() {
            Map<String, ItemStackHandler> slots = Maps.newLinkedHashMap();
            Map<String, CurioType> registry = CuriosAPI.getTypeRegistry();

            for (String id : registry.keySet()) {
                CurioType entry = registry.get(id);

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

                if (CuriosAPI.getType(id) != null) {
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
            CurioType type = CuriosAPI.getType(identifier);

            if (type != null) {
                this.curioSlots.putIfAbsent(identifier, new ItemStackHandler(type.getSize()));
            }
        }

        @Override
        public void removeCurioSlot(String identifier) {
            this.curioSlots.remove(identifier);
        }

        @Nullable
        @Override
        public EntityLivingBase getWearer() {
            return this.wearer;
        }
    }

    public static class Provider implements ICapabilityProvider {

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
            return CURIO_INV_CAP.orEmpty(capability, optional);
        }
    }
}
