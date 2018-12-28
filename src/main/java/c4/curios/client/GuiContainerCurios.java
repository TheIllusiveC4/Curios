package c4.curios.client;

import c4.curios.Curios;
import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurioItemHandler;
import c4.curios.common.inventory.ContainerCurios;
import c4.curios.common.inventory.SlotCurio;
import c4.curios.common.network.NetworkHandler;
import c4.curios.common.network.client.CPacketOpenVanilla;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class GuiContainerCurios extends InventoryEffectRenderer {

    public static final ResourceLocation INVENTORY_BACKGROUND =
            new ResourceLocation(Curios.MODID, "textures/gui/inventory.png");

    private static final Field OLD_MOUSE_X = ReflectionHelper.findField(GuiInventory.class, "oldMouseX",
            "field_147048_u");
    private static final Field OLD_MOUSE_Y = ReflectionHelper.findField(GuiInventory.class, "oldMouseY",
            "field_147047_v");

    /** The old x position of the mouse pointer */
    private float oldMouseX;
    /** The old y position of the mouse pointer */
    private float oldMouseY;
    private boolean widthTooNarrow;
    private ArrowButton prevPageButton;
    private ArrowButton nextPageButton;
    private int totalPages;
    private int currentPage;

    static {
        OLD_MOUSE_X.setAccessible(true);
        OLD_MOUSE_Y.setAccessible(true);
    }

    public GuiContainerCurios(ContainerCurios containerCurios) {
        super(containerCurios);
        this.allowUserInput = true;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    @Override
    public void initGui() {
        this.buttonList.clear();
        super.initGui();
        this.widthTooNarrow = this.width < 379;
        this.guiLeft = (this.width - this.xSize) / 2;
        this.addButton(new GuiButtonImage(44, this.guiLeft + 125, this.height / 2 - 22, 20, 18, 199, 0, 19,
                INVENTORY_BACKGROUND));
        this.totalPages = 1;
        this.currentPage = 1;
        ICurioItemHandler curiosHandler = CuriosAPI.getCuriosHandler(this.mc.player);
        if (curiosHandler != null) {
            this.totalPages = (int)Math.ceil(curiosHandler.getSlots() / 9);
            if (totalPages > 1) {
                this.nextPageButton = this.addButton(new ArrowButton(45, this.guiLeft + 100, this.height / 2 - 22, true));
                this.prevPageButton = this.addButton(new ArrowButton(46, this.guiLeft + 50, this.height / 2 - 22, false));
            }
        }
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format(Curios.MODID + ".gui.container"), 97, 8, 4210752);
        if (this.mc.player.inventory.getItemStack().isEmpty() && this.getSlotUnderMouse() != null) {
            Slot slot = this.getSlotUnderMouse();
            if (slot instanceof SlotCurio && !slot.getHasStack()) {
                SlotCurio slotCurio = (SlotCurio)slot;
                this.drawHoveringText(slotCurio.getSlotName(), mouseX - this.guiLeft, mouseY - this.guiTop);
            }
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.hasActivePotionEffects = true;
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
        this.oldMouseX = (float)mouseX;
        this.oldMouseY = (float)mouseY;
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        GuiInventory.drawEntityOnScreen(i + 51, j + 75, 30, (float)(i + 51) - mouseX,
                (float)(j + 75 - 50) - mouseY, this.mc.player);
        ICurioItemHandler itemHandler = CuriosAPI.getCuriosHandler(this.mc.player);
        if (itemHandler != null) {
            int slotCount = itemHandler.getSlots();
            int yOffset = 17;
            for (int k = 0; k < Math.min(3, slotCount / 3 + 1); k++) {
                int xOffset = 97;
                for (int l = 0; l < Math.min(3, slotCount - (k * 3)); l++) {
                    this.mc.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
                    this.drawTexturedModalRect(i + xOffset, j + yOffset, 179, 38, 18, 18);
                    xOffset += 18;
                }
                yOffset += 18;
            }
        }
    }

    /**
     * Test if the 2D point is in a rectangle (relative to the GUI). Args : rectX, rectY, rectWidth, rectHeight, pointX,
     * pointY
     */
    @Override
    protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY)
    {
        return !this.widthTooNarrow && super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (!this.widthTooNarrow) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 44) {
            GuiInventory inventory = new GuiInventory(this.mc.player);
            try {
                OLD_MOUSE_X.setFloat(inventory, this.oldMouseX);
                OLD_MOUSE_Y.setFloat(inventory, this.oldMouseY);
            } catch(IllegalAccessException e) {
                //Throw error message
            }
            this.mc.displayGuiScreen(inventory);
            NetworkHandler.INSTANCE.sendToServer(new CPacketOpenVanilla());
        }
    }

    @SideOnly(Side.CLIENT)
    static class ArrowButton extends GuiButton {

        private final boolean forward;

        public ArrowButton(int buttonID, int x, int y, boolean forward)
        {
            super(buttonID, x, y, 12, 19, "");
            this.forward = forward;
        }

        /**
         * Draws this button to the screen.
         */
        public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
        {
            if (this.visible)
            {
                mc.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
                int i = 0;
                int j = 220;

                if (!this.enabled)
                {
                    j += this.width * 2;
                }
                else if (flag)
                {
                    j += this.width;
                }

                if (!this.forward)
                {
                    i += this.height;
                }

                this.drawTexturedModalRect(this.x, this.y, j, i, this.width, this.height);
            }
        }
    }
}
