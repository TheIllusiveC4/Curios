package top.theillusivec4.curios.common.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class CrownItem extends Item {

  public CrownItem() {
    super(new Item.Settings().group(ItemGroup.TOOLS).maxCount(1).maxDamage(2000));
  }

  @Override
  public boolean hasGlint(ItemStack stack) {
    return true;
  }
}
