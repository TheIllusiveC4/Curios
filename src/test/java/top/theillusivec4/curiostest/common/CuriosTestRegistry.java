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

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curiostest.CuriosTest;
import top.theillusivec4.curiostest.common.item.AmuletItem;
import top.theillusivec4.curiostest.common.item.CrownItem;
import top.theillusivec4.curiostest.common.item.KnucklesItem;
import top.theillusivec4.curiostest.common.item.RingItem;
import top.theillusivec4.curiostest.common.item.TestArmor;

@Mod.EventBusSubscriber(modid = CuriosTest.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CuriosTestRegistry {

  private static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(ForgeRegistries.ITEMS, CuriosTest.MODID);

  public static final RegistryObject<Item> RING = ITEMS.register("ring", RingItem::new);
  public static final RegistryObject<Item> AMULET = ITEMS.register("amulet", AmuletItem::new);
  public static final RegistryObject<Item> CROWN = ITEMS.register("crown", CrownItem::new);
  public static final RegistryObject<Item> KNUCKLES = ITEMS.register("knuckles", KnucklesItem::new);
  public static final RegistryObject<Item> TEST_ARMOR = ITEMS.register("test_armor",
      () -> new TestArmor(ArmorMaterials.DIAMOND, ArmorItem.Type.CHESTPLATE,
          new Item.Properties()));

  public static void init() {
    ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
  }
}
