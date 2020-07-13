package top.theillusivec4.curios.common.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class AmuletItem extends Item {

  public AmuletItem() {
    super(new Item.Settings().group(ItemGroup.TOOLS).maxCount(1));
  }

  @Override
  public boolean hasGlint(ItemStack stack) {
    return true;
  }
}
