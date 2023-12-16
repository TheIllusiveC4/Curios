/*
 * Copyright (c) 2018-2023 C4
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

package top.theillusivec4.curios.mixin;

import java.util.Map;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CuriosUtilMixinHooks {

  public static boolean canNeutralizePiglins(LivingEntity livingEntity) {
    return CuriosApi.getCuriosInventory(livingEntity).map(handler -> {

      for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
        IDynamicStackHandler stacks = entry.getValue().getStacks();

        for (int i = 0; i < stacks.getSlots(); i++) {
          final int index = i;
          NonNullList<Boolean> renderStates = entry.getValue().getRenders();
          boolean canNeutralize =
              CuriosApi.getCurio(stacks.getStackInSlot(i)).map(curio -> curio
                      .makesPiglinsNeutral(new SlotContext(entry.getKey(), livingEntity, index, false,
                          renderStates.size() > index && renderStates.get(index))))
                  .orElse(false);

          if (canNeutralize) {
            return true;
          }
        }
      }
      return false;
    }).orElse(false);
  }

  public static boolean canWalkOnPowderSnow(LivingEntity livingEntity) {
    return CuriosApi.getCuriosInventory(livingEntity).map(handler -> {

      for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
        IDynamicStackHandler stacks = entry.getValue().getStacks();

        for (int i = 0; i < stacks.getSlots(); i++) {
          final int index = i;
          NonNullList<Boolean> renderStates = entry.getValue().getRenders();
          boolean canWalk =
              CuriosApi.getCurio(stacks.getStackInSlot(i)).map(curio -> curio
                      .canWalkOnPowderedSnow(new SlotContext(entry.getKey(), livingEntity, index, false,
                          renderStates.size() > index && renderStates.get(index))))
                  .orElse(false);

          if (canWalk) {
            return true;
          }
        }
      }
      return false;
    }).orElse(false);
  }

  public static int getFortuneLevel(Player player) {
    return CuriosApi.getCuriosInventory(player)
        .map(handler -> handler.getFortuneLevel(null)).orElse(0);
  }

  public static int getFortuneLevel(LootContext lootContext) {
    Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);

    if (entity instanceof LivingEntity livingEntity) {
      return CuriosApi.getCuriosInventory(livingEntity)
          .map(handler -> handler.getFortuneLevel(lootContext)).orElse(0);
    } else {
      return 0;
    }
  }

  public static boolean isFreezeImmune(LivingEntity livingEntity) {
    return CuriosApi.getCuriosInventory(livingEntity).map(curios -> {
      IItemHandlerModifiable handler = curios.getEquippedCurios();

      for (int i = 0; i < handler.getSlots(); i++) {
        ItemStack stack = handler.getStackInSlot(i);

        if (stack.is(ItemTags.FREEZE_IMMUNE_WEARABLES)) {
          return true;
        }
      }
      return false;
    }).orElse(false);
  }

  public static CompoundTag mergeCuriosInventory(CompoundTag compoundTag, Entity entity) {

    if (entity instanceof LivingEntity livingEntity) {
      ListTag list = compoundTag.getList("Inventory", Tag.TAG_COMPOUND);
      return CuriosApi.getCuriosInventory(livingEntity).map(inv -> {
        IItemHandler handler = inv.getEquippedCurios();

        for (int i = 0; i < handler.getSlots(); i++) {
          ItemStack stack = handler.getStackInSlot(i);

          if (!stack.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            tag.putByte("Slot", (byte) (4444 + i));
            stack.save(tag);
            list.add(tag);
          }
        }
        return compoundTag;
      }).orElse(compoundTag);
    }
    return compoundTag;
  }

  public static boolean containsStack(Player player, ItemStack stack) {
    return CuriosApi.getCuriosInventory(player).map(inv -> inv.findFirstCurio(
            stack2 -> !stack2.isEmpty() && ItemStack.isSameItemSameTags(stack, stack2)).isPresent())
        .orElse(false);
  }

  public static boolean containsTag(Player player, TagKey<Item> tagKey) {
    return CuriosApi.getCuriosInventory(player).map(
            inv -> inv.findFirstCurio(stack2 -> !stack2.isEmpty() && stack2.is(tagKey)).isPresent())
        .orElse(false);
  }
}
