package top.theillusivec4.curios.client.gui;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;

public class RenderButton extends ImageButton {

  public RenderButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn,
      int yDiffTextIn, ResourceLocation resourceLocationIn, Button.IPressable onPressIn) {
    super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn,
        256, 256, onPressIn);
  }

  @Override
  public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
    // NO-OP
  }

  public void renderButtonOverlay(int mouseX, int mouseY, float partialTicks) {
    super.renderButton(mouseX, mouseY, partialTicks);
  }
}
