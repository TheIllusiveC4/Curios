package top.theillusivec4.curios.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import top.theillusivec4.curios.common.inventory.CurioSlot;

public class RenderToggleButton extends TexturedButtonWidget {

  private final Identifier identifier;
  private final int yTexStart;
  private final int xTexStart;
  private final CurioSlot slot;

  public RenderToggleButton(CurioSlot slot, int xIn, int yIn, int widthIn, int heightIn,
      int xTexStartIn, int yTexStartIn, int yDiffTextIn, Identifier identifier,
      ButtonWidget.PressAction onPressIn) {
    super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, identifier, 256, 256,
        onPressIn);
    this.identifier = identifier;
    this.yTexStart = yTexStartIn;
    this.xTexStart = xTexStartIn;
    this.slot = slot;
  }

  @Override
  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    // NO-OP
  }

  public void renderButtonOverlay(MatrixStack matrixStack, int mouseX, int mouseY,
      float partialTicks) {
    MinecraftClient minecraft = MinecraftClient.getInstance();
    minecraft.getTextureManager().bindTexture(this.identifier);
    RenderSystem.disableDepthTest();
    int j = this.xTexStart;

    if (!slot.getRenderStatus()) {
      j += 8;
    }
    drawTexture(matrixStack, this.x, this.y, (float) j, (float) this.yTexStart, this.width,
        this.height, 256, 256);
    RenderSystem.enableDepthTest();
  }
}
