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

package top.theillusivec4.curios.common.item;

import java.util.UUID;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class RingItem extends Item {

  public static final UUID SPEED_UUID = UUID.fromString("8b7c8fcd-89bc-4794-8bb9-eddeb32753a5");
  public static final UUID ARMOR_UUID = UUID.fromString("38faf191-bf78-4654-b349-cc1f4f1143bf");

  public RingItem() {
    super(new Item.Settings().group(ItemGroup.TOOLS).maxCount(1));
  }

  @Override
  public boolean hasGlint(ItemStack stack) {
    return true;
  }
}
