package top.theillusivec4.curios.common.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import top.theillusivec4.curios.api.CuriosApi;

public class AmuletItem extends Item {

  private static final Identifier AMULET_TEXTURE = new Identifier(CuriosApi.MODID,
      "textures/entity/amulet.png");

  public AmuletItem() {
    super(new Item.Settings().group(ItemGroup.TOOLS).maxCount(1).maxDamageIfAbsent(0));
  }

  @Override
  public boolean hasGlint(ItemStack stack) {
    return true;
  }
}
