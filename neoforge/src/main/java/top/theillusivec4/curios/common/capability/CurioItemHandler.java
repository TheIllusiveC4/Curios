package top.theillusivec4.curios.common.capability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.common.CuriosRegistry;

public class CurioItemHandler implements IItemHandler {

  final IItemHandler curios;
  final LivingEntity livingEntity;

  public CurioItemHandler(final LivingEntity livingEntity) {
    this.livingEntity = livingEntity;
    CurioInventory inv = livingEntity.getData(CuriosRegistry.INVENTORY.get());

    if (inv.curiosItemHandler != null) {
      this.curios = inv.curiosItemHandler.getEquippedCurios();
    } else {
      this.curios = new ItemStackHandler();
    }
  }

  @Override
  public int getSlots() {
    return this.curios.getSlots();
  }

  @Override
  public @NotNull ItemStack getStackInSlot(int slot) {
    return this.curios.getStackInSlot(slot);
  }

  @Override
  public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
    return this.curios.insertItem(slot, stack, simulate);
  }

  @Override
  public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
    return this.curios.extractItem(slot, amount, simulate);
  }

  @Override
  public int getSlotLimit(int slot) {
    return this.curios.getSlotLimit(slot);
  }

  @Override
  public boolean isItemValid(int slot, @NotNull ItemStack stack) {
    return this.curios.isItemValid(slot, stack);
  }
}
