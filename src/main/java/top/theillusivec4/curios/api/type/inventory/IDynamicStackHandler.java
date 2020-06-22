package top.theillusivec4.curios.api.type.inventory;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.IItemHandler;

public interface IDynamicStackHandler extends IItemHandler {

  void setStackInSlot(int slot, @Nonnull ItemStack stack);

  @Nonnull
  ItemStack getStackInSlot(int slot);

  void setPreviousStackInSlot(int slot, @Nonnull ItemStack stack);

  ItemStack getPreviousStackInSlot(int slot);

  int getSlots();

  void grow(int amount);

  void shrink(int amount);

  CompoundNBT serializeNBT();

  void deserializeNBT(CompoundNBT nbt);
}
