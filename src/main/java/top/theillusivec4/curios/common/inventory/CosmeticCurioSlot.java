package top.theillusivec4.curios.common.inventory;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CosmeticCurioSlot extends CurioSlot {

  public CosmeticCurioSlot(PlayerEntity player, IDynamicStackHandler handler, int index,
      String identifier, int xPosition, int yPosition) {
    super(player, handler, index, identifier, xPosition, yPosition, null);
    this.setBackground(PlayerContainer.LOCATION_BLOCKS_TEXTURE,
        new ResourceLocation(Curios.MODID, "item/empty_cosmetic_slot"));
  }

  @Override
  public boolean getRenderStatus() {
    return true;
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public String getSlotName() {
    return I18n.format("curios.cosmetic") + " " + super.getSlotName();
  }
}
