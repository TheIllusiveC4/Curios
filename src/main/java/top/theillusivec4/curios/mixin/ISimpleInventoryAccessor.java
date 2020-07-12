package top.theillusivec4.curios.mixin;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleInventory.class)
public interface ISimpleInventoryAccessor {

  @Accessor
  DefaultedList<ItemStack> getStacks();

  @Accessor
  void setStacks(DefaultedList<ItemStack> stacks);
}
