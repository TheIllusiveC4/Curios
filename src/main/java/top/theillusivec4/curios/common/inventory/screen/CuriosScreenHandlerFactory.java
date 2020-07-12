package top.theillusivec4.curios.common.inventory.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class CuriosScreenHandlerFactory implements NamedScreenHandlerFactory {

  @Override
  public Text getDisplayName() {
    return new TranslatableText("container.crafting");
  }

  @Override
  public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
    return new CuriosScreenHandler(syncId, inv);
  }
}
