package top.theillusivec4.curios.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import top.theillusivec4.curios.common.inventory.CurioSlot;

public class RenderButton extends ImageButton {

  private final ResourceLocation resourceLocation;
  private final int yTexStart;
  private final int yDiffText;
  private final int xTexStart;
  private final CurioSlot slot;

  public RenderButton(CurioSlot slot, int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn,
      int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn,
      Button.IPressable onPressIn) {
    super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn,
        256, 256, onPressIn);
    this.resourceLocation = resourceLocationIn;
    this.yTexStart = yTexStartIn;
    this.yDiffText = yDiffTextIn;
    this.xTexStart = xTexStartIn;
    this.slot = slot;
  }

  @Override
  public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
    // NO-OP
  }

  public void renderButtonOverlay() {
    Minecraft minecraft = Minecraft.getInstance();
    minecraft.getTextureManager().bindTexture(this.resourceLocation);
    RenderSystem.disableDepthTest();
    int j = this.xTexStart;

    if (!slot.getRenderStatus()) {
      j += 8;
    }
    blit(this.x, this.y, (float) j, (float) this.yTexStart, this.width, this.height, 256, 256);
    RenderSystem.enableDepthTest();
  }
}
