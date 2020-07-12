package top.theillusivec4.curios.client.screen;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.client.CuriosClientConfig.ButtonCorner;
import top.theillusivec4.curios.common.inventory.screen.CuriosScreenHandler;

public class CuriosScreen extends HandledScreen<CuriosScreenHandler> {

  public static final Identifier CURIO_INVENTORY = new Identifier(CuriosApi.MODID,
      "textures/gui/inventory.png");
  public static final Identifier RECIPE_BUTTON_TEXTURE = new Identifier(
      "minecraft:textures/gui/recipe_button.png");

  public CuriosScreen(CuriosScreenHandler handler, PlayerInventory inventory, Text title) {
    super(handler, inventory, title);
  }

  public static Pair<Integer, Integer> getButtonOffset(boolean isCreative) {
    ButtonCorner corner = ButtonCorner.TOP_LEFT;
    int x = 0;
    int y = 0;

    if (isCreative) {
      x += corner.getCreativeXoffset();
      y += corner.getCreativeYoffset();
    } else {
      x += corner.getXoffset();
      y += corner.getYoffset();
    }
    return new Pair<>(x, y);
  }

  @Override
  protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {

  }
}
