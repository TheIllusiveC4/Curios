package top.theillusivec4.curios.api.type.inventory;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.IItemHandler;

public interface IDynamicStackHandler extends IItemHandler {

  /**
   * Sets a {@link ItemStack} to the given slot index as the current stack.
   *
   * @param slot  The slot index
   * @param stack The {@link ItemStack} to assign as the current stack
   */
  void setStackInSlot(int slot, @Nonnull ItemStack stack);

  /**
   * Gets the {@link ItemStack} assigned as the current stack in the given slot index
   *
   * @param slot The slot index
   * @return The {@link ItemStack} assigned as the current stack
   */
  @Nonnull
  ItemStack getStackInSlot(int slot);

  /**
   * Sets a {@link ItemStack} to the given slot index as the previous stack, for comparison purposes
   * with the current stack.
   *
   * @param slot  The slot index
   * @param stack The {@link ItemStack} to assign as the previous stack
   */
  void setPreviousStackInSlot(int slot, @Nonnull ItemStack stack);

  /**
   * Gets the {@link ItemStack} assigned as the previous stack in the given slot index
   *
   * @param slot The slot index
   * @return The {@link ItemStack} assigned as the previous stack
   */
  ItemStack getPreviousStackInSlot(int slot);

  /**
   * @return The total number of slots
   */
  int getSlots();

  /**
   * Increases the number of slots by the given amount.
   *
   * @param amount The number of slots to add
   */
  void grow(int amount);

  /**
   * Decreases the number of slots by the given amount.
   *
   * @param amount The number of slots to remove
   */
  void shrink(int amount);

  /**
   * Writes the data for this handler.
   *
   * @return A {@link CompoundNBT} representing the serialized data
   */
  CompoundNBT serializeNBT();

  /**
   * Reads the data into this handler.
   *
   * @param nbt A {@link CompoundNBT} representing the serialized data
   */
  void deserializeNBT(CompoundNBT nbt);
}
