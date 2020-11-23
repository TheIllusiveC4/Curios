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

package top.theillusivec4.curios.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public class CuriosCapability {

  @CapabilityInject(ICuriosItemHandler.class)
  public static final Capability<ICuriosItemHandler> INVENTORY;

  @CapabilityInject(ICurio.class)
  public static final Capability<ICurio> ITEM;

  public static final ResourceLocation ID_INVENTORY = new ResourceLocation(CuriosApi.MODID,
      "inventory");
  public static final ResourceLocation ID_ITEM = new ResourceLocation(CuriosApi.MODID, "item");

  public static ICapabilityProvider createSimpleProvider(ICurio curio) {
    return new ICapabilityProvider() {
      final LazyOptional<ICurio> lazyCurio = LazyOptional.of(() -> curio);

      @Nonnull
      @Override
      public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable
          Direction side) {
        return ITEM.orEmpty(cap, this.lazyCurio);
      }
    };
  }

  static {
    INVENTORY = null;
    ITEM = null;
  }
}
