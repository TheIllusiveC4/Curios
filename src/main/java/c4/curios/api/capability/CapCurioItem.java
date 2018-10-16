package c4.curios.api.capability;

import c4.curios.Curios;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.*;

import javax.annotation.Nullable;

public class CapCurioItem {

    @CapabilityInject(ICurio.class)
    public static final Capability<ICurio> CURIO_CAP = null;

    public static final EnumFacing DEFAULT_FACING = null;
    public static final ResourceLocation ID = new ResourceLocation(Curios.MODID, "curio_item");

    public static void register() {
        CapabilityManager.INSTANCE.register(ICurio.class, new Capability.IStorage<ICurio>() {
            @Override
            public NBTBase writeNBT(Capability<ICurio> capability, ICurio instance, EnumFacing side) {
                return new NBTTagCompound();
            }

            @Override
            public void readNBT(Capability<ICurio> capability, ICurio instance, EnumFacing side, NBTBase nbt) {

            }
        }, CurioWrapper::new);
    }

    public static ICapabilityProvider createProvider(final ICurio curio) {
        return new Provider(curio, CURIO_CAP, DEFAULT_FACING);
    }

    public static class CurioWrapper implements ICurio {}

    public static class Provider implements ICapabilitySerializable<NBTBase> {

        final Capability<ICurio> capability;
        final EnumFacing facing;
        final ICurio instance;

        Provider(final ICurio instance, final Capability<ICurio> capability, @Nullable final EnumFacing facing) {
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

        final Capability<ICurio> getCapability() {
            return capability;
        }

        EnumFacing getFacing() {
            return facing;
        }

        final ICurio getInstance() {
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