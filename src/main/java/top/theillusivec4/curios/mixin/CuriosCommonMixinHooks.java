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

package top.theillusivec4.curios.mixin;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CuriosCommonMixinHooks {

  public static Pair<String, TypeTemplate>[] attachDataFixer(Schema schema,
                                                             Pair<String, TypeTemplate>[] original) {
    return ArrayUtils.add(original,
        Pair.of("neoforge:attachments",
            DSL.optionalFields("curios:inventory",
                DSL.optionalFields("Curios",
                    DSL.list(
                        DSL.optionalFields("StacksHandler",
                            DSL.optionalFields("Stacks",
                                DSL.optionalFields("Items",
                                    DSL.list(References.ITEM_STACK.in(schema))
                                )
                            )
                        )
                    )
                )
            )
        ));
  }

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

  public static int getFortuneLevel(LootContext lootContext) {
    Entity entity = lootContext.getOptionalParameter(LootContextParams.THIS_ENTITY);

    if (entity instanceof LivingEntity livingEntity) {
      return CuriosApi.getCuriosInventory(livingEntity)
          .map(handler -> handler.getFortuneLevel(lootContext)).orElse(0);
    }
    return 0;
  }

  public static int getLootingLevel(LootContext lootContext) {
    Entity entity = lootContext.getOptionalParameter(LootContextParams.ATTACKING_ENTITY);

    if (entity instanceof LivingEntity livingEntity) {
      return CuriosApi.getCuriosInventory(livingEntity)
          .map(handler -> handler.getLootingLevel(lootContext)).orElse(0);
    }
    return 0;
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
      ListTag list = compoundTag.getList("Inventory").orElse(new ListTag());
      return CuriosApi.getCuriosInventory(livingEntity).map(inv -> {
        IItemHandler handler = inv.getEquippedCurios();

        for (int i = 0; i < handler.getSlots(); i++) {
          ItemStack stack = handler.getStackInSlot(i);

          if (!stack.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            tag.putByte("Slot", (byte) (4444 + i));
            list.add(stack.save(livingEntity.registryAccess(), tag));
          }
        }
        return compoundTag;
      }).orElse(compoundTag);
    }
    return compoundTag;
  }

  public static boolean containsStack(Player player, ItemStack stack) {
    return CuriosApi.getCuriosInventory(player).flatMap(inv -> inv.findFirstCurio(
            stack2 -> !stack2.isEmpty() && ItemStack.isSameItemSameComponents(stack, stack2)))
        .isPresent();
  }

  public static boolean containsTag(Player player, TagKey<Item> tagKey) {
    return CuriosApi.getCuriosInventory(player).flatMap(
            inv -> inv.findFirstCurio(stack2 -> !stack2.isEmpty() && stack2.is(tagKey)))
        .isPresent();
  }

  public static boolean contains(Player player, Predicate<ItemStack> predicate) {
    return CuriosApi.getCuriosInventory(player).flatMap(inv -> inv.findFirstCurio(predicate)).isPresent();
  }
}
