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

package top.theillusivec4.curiostest.common;

import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import top.theillusivec4.curiostest.CuriosTest;
import top.theillusivec4.curiostest.common.item.AmuletItem;
import top.theillusivec4.curiostest.common.item.CrownItem;
import top.theillusivec4.curiostest.common.item.KnucklesItem;
import top.theillusivec4.curiostest.common.item.RingItem;

@Mod.EventBusSubscriber(modid = CuriosTest.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CuriosTestRegistry {

  @ObjectHolder("curiostest:ring")
  public static final Item RING;

  @ObjectHolder("curiostest:amulet")
  public static final Item AMULET;

  @ObjectHolder("curiostest:crown")
  public static final Item CROWN;

  @ObjectHolder("curiostest:knuckles")
  public static final Item KNUCKLES;

  static {
    RING = null;
    AMULET = null;
    CROWN = null;
    KNUCKLES = null;
  }

  @SubscribeEvent
  public static void registerItems(RegistryEvent.Register<Item> evt) {
    IForgeRegistry<Item> registry = evt.getRegistry();
    register(new RingItem(), "ring", registry);
    register(new AmuletItem(), "amulet", registry);
    register(new CrownItem(), "crown", registry);
    register(new KnucklesItem(), "knuckles", registry);
  }

  private static void register(Item item, String name, IForgeRegistry<Item> registry) {
    item.setRegistryName(CuriosTest.MODID, name);
    registry.register(item);
  }
}
