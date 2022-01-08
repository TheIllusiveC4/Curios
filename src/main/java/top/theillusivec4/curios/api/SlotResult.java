package top.theillusivec4.curios.api;

import net.minecraft.item.ItemStack;

public final class SlotResult {

  private final SlotContext slotContext;
  private final ItemStack stack;

  public SlotResult(SlotContext slotContext, ItemStack stack) {
    this.slotContext = slotContext;
    this.stack = stack;
  }

  public SlotContext getSlotContext() {
    return slotContext;
  }

  public ItemStack getStack() {
    return stack;
  }
}
