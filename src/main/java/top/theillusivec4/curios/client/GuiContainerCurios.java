package top.theillusivec4.curios.client;

import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.common.inventory.ContainerCurios;
import top.theillusivec4.curios.common.inventory.SlotCurio;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketOpenVanilla;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class GuiContainerCurios extends InventoryEffectRenderer {

    public static final ResourceLocation CURIO_INVENTORY = new ResourceLocation(Curios.MODID, "textures/gui/inventory.png");

    private static final ResourceLocation CREATIVE_INVENTORY_TABS = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

    private float oldMouseX;
    private float oldMouseY;
    private boolean widthTooNarrow;
    private float currentScroll;
    private boolean isScrolling;
    private boolean wasClicking;
    private boolean buttonClicked;

    public GuiContainerCurios(ContainerCurios containerCurios) {
        super(containerCurios);
        this.allowUserInput = true;
    }

    @Override
    public void tick() {
        if (this.mc.playerController.isInCreativeMode()) {
            this.mc.displayGuiScreen(new GuiContainerCreative(this.mc.player));
        }
    }

    @Override
    public void initGui() {
        if (this.mc.playerController.isInCreativeMode()) {
            this.mc.displayGuiScreen(new GuiContainerCreative(this.mc.player));
        } else {
            super.initGui();
            this.widthTooNarrow = this.width < 379;
            this.guiLeft = (this.width - this.xSize) / 2;
            this.addButton(new GuiButtonImage(44, this.guiLeft + 125, this.height / 2 - 22, 20,
                    18, 50, 0, 19, CURIO_INVENTORY) {

                @Override
                public void onClick(double mouseX, double mouseY) {
                    GuiInventory inventory = new GuiInventory(GuiContainerCurios.this.mc.player);
                    GuiContainerCurios.this.mc.displayGuiScreen(inventory);
                    NetworkHandler.INSTANCE.sendToServer(new CPacketOpenVanilla());
                }
            });
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
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
        super.render(mouseX, mouseY, partialTicks);
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
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GuiInventory.INVENTORY_BACKGROUND);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        GuiInventory.drawEntityOnScreen(i + 51, j + 75, 30, (float)(i + 51) - mouseX,
                (float)(j + 75 - 50) - mouseY, this.mc.player);
        CuriosAPI.getCuriosHandler(this.mc.player).ifPresent(handler -> {
            int slotCount = handler.getSlots();
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
        });
    }

    /**
     * Test if the 2D point is in a rectangle (relative to the GUI). Args : rectX, rectY, rectWidth, rectHeight, pointX,
     * pointY
     */
    @Override
    protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, double pointX, double pointY) {
        return !this.widthTooNarrow && super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return this.widthTooNarrow && super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseReleased1, double mouseReleased3, int mouseReleased5) {

        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        } else {
            return super.mouseReleased(mouseReleased1, mouseReleased3, mouseReleased5);
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
}
