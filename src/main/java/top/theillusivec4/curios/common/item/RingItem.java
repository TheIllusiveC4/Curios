package top.theillusivec4.curios.common.item;

import java.util.UUID;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class RingItem extends Item {

  public static final UUID SPEED_UUID = UUID.fromString("8b7c8fcd-89bc-4794-8bb9-eddeb32753a5");
  public static final UUID ARMOR_UUID = UUID.fromString("38faf191-bf78-4654-b349-cc1f4f1143bf");

  public RingItem() {
    super(new Item.Settings().group(ItemGroup.TOOLS).maxCount(1));
  }

  @Override
  public boolean hasGlint(ItemStack stack) {
    return true;
  }
}
