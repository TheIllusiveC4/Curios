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

package top.theillusivec4.curiostest.common.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curiostest.CuriosTest;

public class KnucklesItem extends Item {

  public KnucklesItem() {
    super(new Item.Properties().stacksTo(1));
  }

  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag unused) {
    return CuriosApi.createCurioProvider(new ICurio() {

      @Override
      public ItemStack getStack() {
        return stack;
      }

      @Override
      public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext,
                                                                          UUID uuid) {
        Multimap<Attribute, AttributeModifier> atts = LinkedHashMultimap.create();
        atts.put(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(uuid, CuriosTest.MODID + ":attack_damage_bonus", 4,
                AttributeModifier.Operation.ADDITION));
        CuriosApi.addSlotModifier(atts, "necklace", uuid, 2, AttributeModifier.Operation.ADDITION);
        CuriosApi.addSlotModifier(atts, "ring", uuid, -1, AttributeModifier.Operation.ADDITION);
        return atts;
      }
    });
  }

  @Override
  public boolean isFoil(@Nonnull ItemStack stack) {
    return true;
  }
}
