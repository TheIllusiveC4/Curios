/*
 * Copyright (c) 2018-2024 C4
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
 *
 */

package top.theillusivec4.curios.server.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public class CuriosSelectorOptions {

  public static void register() {
    EntitySelectorOptions.register("curios", CuriosSelectorOptions::curioArgument,
                                   entitySelectorParser -> true,
                                   Component.translatable(
                                       "argument.entity.options.curios.description"));
  }

  private static void curioArgument(EntitySelectorParser parser) throws CommandSyntaxException {
    StringReader reader = parser.getReader();
    boolean invert = parser.shouldInvertValue();
    Tag tag = TagParser.create(NbtOps.INSTANCE).parseFully(reader);

    if (!(tag instanceof CompoundTag compoundtag)) {
      return;
    }
    ListTag listTag = compoundtag.getList("slot").orElse(new ListTag());
    Set<String> slots = new HashSet<>();

    for (int i = 0; i < listTag.size(); i++) {
      slots.add(listTag.getString(i).orElse(""));
    }
    listTag = compoundtag.getList("index").orElse(new ListTag());
    int min = 0;
    int max = -1;

    if (listTag.size() == 2) {
      min = Math.max(0, listTag.getInt(0).orElse(min));
      max = Math.max(min + 1, listTag.getInt(1).orElse(max));
    }
    CompoundTag stack =
        compoundtag.contains("item") ? compoundtag.getCompound("item").orElse(new CompoundTag())
                                     : new CompoundTag();
    boolean exclusive = compoundtag.getBoolean("exclusive").orElse(false);
    int finalMin = min;
    int finalMax = max;
    parser.addPredicate(
        entity -> matches(entity, slots, finalMin, finalMax, stack, invert, exclusive));
  }

  private static boolean matches(Entity entity, Set<String> slots, int min, int max,
                                 CompoundTag inputStack, boolean invert, boolean exclusive) {
    if (entity instanceof LivingEntity livingEntity) {
      ItemStack stack =
          ItemStack.parse(livingEntity.registryAccess(), inputStack).orElse(ItemStack.EMPTY);

      if (!stack.isEmpty()) {
        stack.setCount(Math.max(1, stack.getCount()));
      }
      return CuriosApi.getCuriosInventory(livingEntity).map(handler -> {
        Map<String, ICurioStacksHandler> curios = handler.getCurios();

        if (!stack.isEmpty()) {

          if (exclusive) {
            return hasOnlyItem(curios, slots, min, max, stack, invert);
          } else {
            return hasItem(curios, slots, min, max, stack, invert);
          }
        } else if (!slots.isEmpty()) {

          if (exclusive) {
            return hasOnlySlot(curios, slots, max, invert);
          } else {
            return hasSlot(curios, slots, max, invert);
          }
        }
        return true;
      }).orElse(false);
    } else {
      return false;
    }
  }

  private static boolean hasOnlySlot(Map<String, ICurioStacksHandler> curios, Set<String> slots,
                                     int max, boolean invert) {

    boolean foundSlot = false;

    if (invert) {

      for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {

        if (matches(slots, max, entry.getKey(), entry.getValue())) {
          foundSlot = true;
        } else if (foundSlot) {
          return true;
        }
      }
      return false;
    } else {

      for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {

        if (matches(slots, max, entry.getKey(), entry.getValue())) {
          foundSlot = true;
        } else if (foundSlot) {
          return false;
        }
      }
      return foundSlot;
    }
  }

  private static boolean hasSlot(Map<String, ICurioStacksHandler> curios, Set<String> slots,
                                 int max, boolean invert) {

    for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {

      if (matches(slots, max, entry.getKey(), entry.getValue())) {
        return !invert;
      }
    }
    return invert;
  }

  private static boolean matches(Set<String> slots, int max, String id,
                                 ICurioStacksHandler stacks) {
    int size = stacks.getSlots();
    return slots.contains(id) && size > 0 && (max == -1 || size >= max);
  }

  private static boolean hasOnlyItem(Map<String, ICurioStacksHandler> curios, Set<String> slots,
                                     int min, int max, ItemStack stack, boolean invert) {

    boolean foundItem = false;

    if (invert) {

      for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {

        if (slots.isEmpty() || slots.contains(entry.getKey())) {
          ICurioStacksHandler stacks = entry.getValue();
          int limit = max == -1 ? stacks.getSlots() : Math.min(stacks.getSlots(), max);

          for (int i = min; i < limit; i++) {
            ItemStack current = stacks.getStacks().getStackInSlot(i);

            if (ItemStack.matches(current, stack)) {
              foundItem = true;
            } else if (foundItem) {
              return true;
            }
          }
        }
      }
      return false;
    } else {

      for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {

        if (slots.isEmpty() || slots.contains(entry.getKey())) {
          ICurioStacksHandler stacks = entry.getValue();
          int limit = max == -1 ? stacks.getSlots() : Math.min(stacks.getSlots(), max);

          for (int i = min; i < limit; i++) {
            ItemStack current = stacks.getStacks().getStackInSlot(i);

            if (ItemStack.matches(current, stack)) {
              foundItem = true;
            } else if (foundItem) {
              return false;
            }
          }
        }
      }
      return foundItem;
    }
  }

  private static boolean hasItem(Map<String, ICurioStacksHandler> curios, Set<String> slots,
                                 int min, int max, ItemStack stack, boolean invert) {

    for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {

      if (slots.isEmpty() || slots.contains(entry.getKey())) {
        ICurioStacksHandler stacks = entry.getValue();
        int limit = max == -1 ? stacks.getSlots() : Math.min(stacks.getSlots(), max);

        for (int i = min; i < limit; i++) {
          ItemStack current = stacks.getStacks().getStackInSlot(i);

          if (ItemStack.matches(current, stack)) {
            return !invert;
          }
        }
      }
    }
    return invert;
  }
}
