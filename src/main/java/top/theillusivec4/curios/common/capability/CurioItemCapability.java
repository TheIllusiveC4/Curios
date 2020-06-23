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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class CurioItemCapability {

  public static void register() {
    CapabilityManager.INSTANCE.register(ICurio.class, new Capability.IStorage<ICurio>() {
      @Override
      public INBT writeNBT(Capability<ICurio> capability, ICurio instance, Direction side) {
        return new CompoundNBT();
      }

      @Override
      public void readNBT(Capability<ICurio> capability, ICurio instance, Direction side,
          INBT nbt) {
      }
    }, CurioItemWrapper::new);
  }

  public static ICapabilityProvider createProvider(final ICurio curio) {
    return new Provider(curio);
  }

  private static class CurioItemWrapper implements ICurio {

  }

  public static class Provider implements ICapabilityProvider {

    final LazyOptional<ICurio> capability;

    Provider(ICurio curio) {
      this.capability = LazyOptional.of(() -> curio);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
      return CuriosCapability.ITEM.orEmpty(cap, capability);
    }
  }
}