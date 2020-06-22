package top.theillusivec4.curios.api.type.inventory;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;

public interface ICurioStacksHandler {

  IDynamicStackHandler getStacks();

  IDynamicStackHandler getCosmeticStacks();

  NonNullList<Boolean> getRenders();

  int getSlots();

  int getSizeShift();

  void grow(int amount);

  void shrink(int amount);

  CompoundNBT serializeNBT();

  void deserializeNBT(CompoundNBT nbt);
}
