package top.theillusivec4.curios.common.capability;

import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.CuriosRegistry;

public class CurioItemHandler implements IItemHandler {

  final IItemHandler curios;
  final LivingEntity livingEntity;

  public CurioItemHandler(final LivingEntity livingEntity) {
    this.livingEntity = livingEntity;
    CurioInventory inv = livingEntity.getData(CuriosRegistry.INVENTORY.get());
    Map<String, ICurioStacksHandler> curios = inv.curios;
    IItemHandlerModifiable[] itemHandlers = new IItemHandlerModifiable[curios.size()];
    int index = 0;

    for (ICurioStacksHandler stacksHandler : curios.values()) {

      if (index < itemHandlers.length) {
        itemHandlers[index] = stacksHandler.getStacks();
        index++;
      }
    }
    this.curios = new CombinedInvWrapper(itemHandlers);
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
