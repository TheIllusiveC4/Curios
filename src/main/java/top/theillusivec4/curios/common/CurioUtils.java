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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import top.theillusivec4.curios.api.CuriosAPI;

public class CurioUtils {

  public static Multimap<String, AttributeModifier> getAttributeModifiers(String identifier,
      ItemStack stack) {
    Multimap<String, AttributeModifier> multimap;
    if (stack.getTag() != null && stack.getTag().contains("CurioAttributeModifiers", 9)) {
      multimap = HashMultimap.create();
      ListNBT listnbt = stack.getTag().getList("CurioAttributeModifiers", 10);

      for (int i = 0; i < listnbt.size(); ++i) {
        CompoundNBT compoundnbt = listnbt.getCompound(i);
        AttributeModifier attributemodifier = SharedMonsterAttributes
            .readAttributeModifier(compoundnbt);
        if (attributemodifier != null && (!compoundnbt.contains("Slot", 8) || compoundnbt
            .getString("Slot").equals(identifier))
            && attributemodifier.getID().getLeastSignificantBits() != 0L
            && attributemodifier.getID().getMostSignificantBits() != 0L) {
          multimap.put(compoundnbt.getString("AttributeName"), attributemodifier);
        }
      }
      return multimap;
    }
    return CuriosAPI.getCurio(stack).map(curio -> curio.getAttributeModifiers(identifier))
        .orElse(HashMultimap.create());
  }
}
