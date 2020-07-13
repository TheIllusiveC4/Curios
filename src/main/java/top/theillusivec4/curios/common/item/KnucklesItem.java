package top.theillusivec4.curios.common.item;

import java.util.UUID;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class KnucklesItem extends Item {

  public static final UUID ATTACK_DAMAGE_UUID = UUID
      .fromString("7ce10414-adcc-4bf2-8804-f5dbd39fadaf");

  public KnucklesItem() {
    super(new Item.Settings().group(ItemGroup.TOOLS).maxCount(1));
  }

  @Override
  public boolean hasGlint(ItemStack stack) {
    return true;
  }
}
