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

package top.theillusivec4.curios.common;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

@Mod.EventBusSubscriber(modid = Curios.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CuriosRegistry {

  @ObjectHolder("curios:curios_container")
  public static final MenuType<CuriosContainer> CONTAINER_TYPE;

  static {
    CONTAINER_TYPE = null;
  }

  @SubscribeEvent
  public static void registerContainer(RegistryEvent.Register<MenuType<?>> evt) {
    evt.getRegistry().register(
        IForgeMenuType.create(CuriosContainer::new).setRegistryName("curios_container"));
  }
}
