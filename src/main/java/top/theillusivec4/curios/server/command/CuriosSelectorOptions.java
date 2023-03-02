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
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public class CuriosSelectorOptions {

  public static void register() {
    EntitySelectorOptions.register("curios", CuriosSelectorOptions::curioArgument,
        entitySelectorParser -> true, new TranslatableComponent("argument.entity.options.curios.description"));
  }

  private static void curioArgument(EntitySelectorParser parser) throws CommandSyntaxException {
    StringReader reader = parser.getReader();
    boolean invert = parser.shouldInvertValue();
    CompoundTag compoundtag = (new TagParser(reader)).readStruct();
    ListTag listTag = compoundtag.getList("slot", Tag.TAG_STRING);
    Set<String> slots = new HashSet<>();

    for (int i = 0; i < listTag.size(); i++) {
      slots.add(listTag.getString(i));
    }
    listTag = compoundtag.getList("index", Tag.TAG_INT);
    int min = 0;
    int max = -1;

    if (listTag.size() == 2) {
      min = Math.max(0, listTag.getInt(0));
      max = Math.max(min + 1, listTag.getInt(1));
    }
    ItemStack stack =
        compoundtag.contains("item") ? ItemStack.of(compoundtag.getCompound("item")) : null;

    if (stack != null) {
      stack.setCount(Math.max(1, stack.getCount()));
    }
    boolean exclusive = compoundtag.getBoolean("exclusive");
    int finalMin = min;
    int finalMax = max;
    parser.addPredicate(
        entity -> matches(entity, slots, finalMin, finalMax, stack, invert, exclusive));
  }

  private static boolean matches(Entity entity, Set<String> slots, int min, int max,
                                 ItemStack stack,
                                 boolean invert, boolean exclusive) {
    if (entity instanceof LivingEntity livingEntity) {
      return CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).map(handler -> {
        Map<String, ICurioStacksHandler> curios = handler.getCurios();

        if (stack != null) {

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
