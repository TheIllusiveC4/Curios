package top.theillusivec4.curios.common.capability;

import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.ICurio;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapCurioItem {

    public static void register() {
        CapabilityManager.INSTANCE.register(ICurio.class, new Capability.IStorage<ICurio>() {
            @Override
            public INBTBase writeNBT(Capability<ICurio> capability, ICurio instance, EnumFacing side) {
                return new NBTTagCompound();
            }

            @Override
            public void readNBT(Capability<ICurio> capability, ICurio instance, EnumFacing side, INBTBase nbt) {

            }
        }, CurioWrapper::new);
    }

    public static ICapabilityProvider createProvider(final ICurio curio) {
        return new Provider(curio);
    }

    private static class CurioWrapper implements ICurio {}

    public static class Provider implements ICapabilityProvider {

        final LazyOptional<ICurio> capability;

        Provider(ICurio curio) {
            this.capability = LazyOptional.of(() -> curio);
        }

        @SuppressWarnings("ConstantConditions")
        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
            return CuriosCapability.ITEM.orEmpty(cap, capability);
        }
    }
}