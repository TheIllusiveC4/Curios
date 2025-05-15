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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.theillusivec4.curiostest.CuriosTest;
import top.theillusivec4.curiostest.common.item.AmuletItem;
import top.theillusivec4.curiostest.common.item.CrownItem;
import top.theillusivec4.curiostest.common.item.KnucklesItem;
import top.theillusivec4.curiostest.common.item.RingItem;
import top.theillusivec4.curiostest.common.item.TestArmor;

public class CuriosTestRegistry {

  private static final DeferredRegister.Items ITEMS =
      DeferredRegister.createItems(CuriosTest.MODID);

  public static final DeferredItem<Item> RING =
      ITEMS.registerItem("ring", RingItem::new, new Item.Properties().stacksTo(1).durability(0));
  public static final DeferredItem<Item> AMULET = ITEMS.registerItem("amulet", AmuletItem::new,
                                                                     new Item.Properties().stacksTo(
                                                                         1).durability(0));
  public static final DeferredItem<Item> CROWN = ITEMS.registerItem("crown", CrownItem::new,
                                                                    new Item.Properties().stacksTo(
                                                                        1).durability(2000));
  public static final DeferredItem<Item> KNUCKLES =
      ITEMS.registerItem("knuckles", KnucklesItem::new, new Item.Properties().stacksTo(1));

  public static final DeferredItem<Item> TEST_ARMOR =
      ITEMS.registerItem("test_armor", TestArmor::new);

  public static void init(IEventBus eventBus) {
    ITEMS.register(eventBus);
  }
}
