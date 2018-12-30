package c4.curios.client;

import c4.curios.Curios;
import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurioItemHandler;
import c4.curios.common.inventory.ContainerCurios;
import c4.curios.common.inventory.SlotCurio;
import c4.curios.common.network.NetworkHandler;
import c4.curios.common.network.client.CPacketOpenVanilla;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class GuiContainerCurios extends InventoryEffectRenderer {

    public static final ResourceLocation CURIO_INVENTORY = new ResourceLocation(Curios.MODID, "textures/gui/inventory.png");

    private static final ResourceLocation CREATIVE_INVENTORY_TABS = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    private static final Field OLD_MOUSE_X = ReflectionHelper.findField(GuiInventory.class, "oldMouseX",
            "field_147048_u");
    private static final Field OLD_MOUSE_Y = ReflectionHelper.findField(GuiInventory.class, "oldMouseY",
            "field_147047_v");

    /** The old x position of the mouse pointer */
    private float oldMouseX;
    /** The old y position of the mouse pointer */
    private float oldMouseY;
    private boolean widthTooNarrow;

    /** Amount scrolled in Creative mode inventory (0 = top, 1 = bottom) */
    private float currentScroll;
    /** True if the scrollbar is being dragged */
    private boolean isScrolling;
    /** True if the left mouse button was held down last time drawScreen was called. */
    private boolean wasClicking;

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
        this.addButton(new GuiButtonImage(44, this.guiLeft + 125, this.height / 2 - 22, 20,
                18, 50, 0, 19, CURIO_INVENTORY));
        this.addButton(new GuiButtonImage(10, this.guiLeft + 104, this.height / 2 - 22, 20,
                18, 178, 0, 19, INVENTORY_BACKGROUND));
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.hasActivePotionEffects = false;
        boolean flag = Mouse.isButtonDown(0);
        int i = this.guiLeft;
        int j = this.guiTop;
        int k = i - 34;
        int l = j + 12;
        int i1 = k + 14;
        int j1 = l + 139;

        if (!this.wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1) {
            this.isScrolling = this.needsScrollBars();
        }

        if (!flag) {
            this.isScrolling = false;
        }
        this.wasClicking = flag;

        if (this.isScrolling) {
            this.currentScroll = ((float)(mouseY - l) - 7.5F) / ((float)(j1 - l) - 15.0F);
            this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
            ((ContainerCurios)this.inventorySlots).scrollTo(this.currentScroll);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
        this.oldMouseX = (float)mouseX;
        this.oldMouseY = (float)mouseY;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("container.crafting"), 97, 8, 4210752);
        if (this.mc.player.inventory.getItemStack().isEmpty() && this.getSlotUnderMouse() != null) {
            Slot slot = this.getSlotUnderMouse();
            if (slot instanceof SlotCurio && !slot.getHasStack()) {
                SlotCurio slotCurio = (SlotCurio)slot;
                this.drawHoveringText(slotCurio.getSlotName(), mouseX - this.guiLeft, mouseY - this.guiTop);
            }
        }
    }


    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GuiInventory.INVENTORY_BACKGROUND);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        GuiInventory.drawEntityOnScreen(i + 51, j + 75, 30, (float)(i + 51) - mouseX,
                (float)(j + 75 - 50) - mouseY, this.mc.player);
        ICurioItemHandler itemHandler = CuriosAPI.getCuriosHandler(this.mc.player);
        if (itemHandler != null) {
            int slotCount = itemHandler.getSlots();
            int upperHeight = 7 + slotCount * 18;
            this.mc.getTextureManager().bindTexture(CURIO_INVENTORY);
            this.drawTexturedModalRect(i - 26, j + 4, 0, 0, 27, upperHeight);

            if (slotCount <= 8) {
                this.drawTexturedModalRect(i - 26, j + 4 + upperHeight, 0, 151, 27, 7);
            } else {
                this.drawTexturedModalRect(i - 42, j + 4, 27, 0, 23, 158);
                this.mc.getTextureManager().bindTexture(CREATIVE_INVENTORY_TABS);
                this.drawTexturedModalRect(i - 34, j + 12 + (int)(127f * this.currentScroll), 232, 0, 12, 15);
            }
        }
    }

    /**
     * Test if the 2D point is in a rectangle (relative to the GUI). Args : rectX, rectY, rectWidth, rectHeight, pointX,
     * pointY
     */
    @Override
    protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
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

    private boolean needsScrollBars() {
        return ((ContainerCurios)this.inventorySlots).canScroll();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        if (i != 0 && this.needsScrollBars()) {
            int j = (((ContainerCurios)this.inventorySlots).curios.getSlots());

            if (i > 0) {
                i = 1;
            }

            if (i < 0) {
                i = -1;
            }

            this.currentScroll = (float)((double)this.currentScroll - (double)i / (double)j);
            this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
            ((ContainerCurios)this.inventorySlots).scrollTo(this.currentScroll);
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
}
