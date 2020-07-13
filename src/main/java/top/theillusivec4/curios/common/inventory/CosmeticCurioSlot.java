package top.theillusivec4.curios.common.inventory;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CosmeticCurioSlot extends CurioSlot {

  public CosmeticCurioSlot(PlayerEntity player, IDynamicStackHandler handler, int index,
      String identifier, int xPosition, int yPosition) {
    super(player, handler, index, identifier, xPosition, yPosition, null);
  }

  @Environment(EnvType.CLIENT)
  @Override
  public Pair<Identifier, Identifier> getBackgroundSprite() {
    return new Pair<>(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
        new Identifier(CuriosApi.MODID, "item/empty_cosmetic_slot"));
  }

  @Override
  public boolean getRenderStatus() {
    return true;
  }

  @Environment(EnvType.CLIENT)
  @Override
  public String getSlotName() {
    return I18n.translate("curios.cosmetic") + " " + super.getSlotName();
  }
}
