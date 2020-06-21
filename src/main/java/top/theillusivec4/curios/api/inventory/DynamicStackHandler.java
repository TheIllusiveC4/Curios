package top.theillusivec4.curios.api.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class DynamicStackHandler extends ItemStackHandler {

  public DynamicStackHandler(int size) {
    super(size);
  }

  public void grow(int amount) {

    for (int i = 0; i < amount; i++) {
      this.stacks.add(ItemStack.EMPTY);
    }
  }

  public void shrink(int amount) {
    int targetSize = this.stacks.size() - amount;

    while (this.stacks.size() > targetSize) {
      this.stacks.remove(this.stacks.size() - 1);
    }
  }
}
