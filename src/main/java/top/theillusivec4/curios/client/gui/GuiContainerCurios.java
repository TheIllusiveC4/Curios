/*
 * Copyright (C) 2018-2019  C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.curios.client.gui;

import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.ICurioItemHandler;
import top.theillusivec4.curios.api.inventory.SlotCurio;
import top.theillusivec4.curios.client.KeyRegistry;
import top.theillusivec4.curios.common.inventory.ContainerCurios;

@OnlyIn(Dist.CLIENT)
public class GuiContainerCurios extends InventoryEffectRenderer {

    static final ResourceLocation CURIO_INVENTORY = new ResourceLocation(Curios.MODID, "textures/gui/inventory.png");

    private static final ResourceLocation CREATIVE_INVENTORY_TABS = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

    public float oldMouseX;
    public float oldMouseY;

    private boolean widthTooNarrow;
    private float currentScroll;
    private boolean isScrolling;
    private boolean buttonClicked;

    public GuiContainerCurios(ContainerCurios containerCurios, float oldMouseX, float oldMouseY) {
        super(containerCurios);
        this.allowUserInput = true;
        this.oldMouseX = oldMouseX;
        this.oldMouseY = oldMouseY;
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
            this.addButton(new GuiButtonCurios(this,44, this.getGuiLeft() + 26, this.height / 2 - 75,
                    14, 14, 50, 0, 14, CURIO_INVENTORY));
        }
    }

    private boolean inScrollBar(double mouseX, double mouseY) {
        int i = this.guiLeft;
        int j = this.guiTop;
        int k = i - 34;
        int l = j + 12;
        int i1 = k + 14;
        int j1 = l + 139;
        return mouseX >= (double)k && mouseY >= (double)l && mouseX < (double)i1 && mouseY < (double)j1;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.hasActivePotionEffects = false;
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
        oldMouseX = (float)mouseX;
        oldMouseY = (float)mouseY;
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {

        if (KeyRegistry.openCurios.isActiveAndMatches(InputMappings.getInputByCode(p_keyPressed_1_, p_keyPressed_2_))) {
            this.mc.player.closeScreen();
            return true;
        } else {
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
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
     * Draws the background layer of this container (behind the item).
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GuiInventory.INVENTORY_BACKGROUND);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        GuiInventory.drawEntityOnScreen(i + 51, j + 75, 30, (float)(i + 51) - oldMouseX,
                (float)(j + 75 - 50) - oldMouseY, this.mc.player);
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

        if (this.inScrollBar(mouseX, mouseY)) {
            this.isScrolling = this.needsScrollBars();
            return true;
        }
        return !this.widthTooNarrow && super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseReleased1, double mouseReleased3, int mouseReleased5) {

        if (mouseReleased5 == 0) {
            this.isScrolling = false;
        }

        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        } else {
            return super.mouseReleased(mouseReleased1, mouseReleased3, mouseReleased5);
        }
    }

    @Override
    public boolean mouseDragged(double pMouseDragged1, double pMouseDragged3, int pMouseDragged5, double pMouseDragged6, double pMouseDragged8) {
        if (this.isScrolling) {
            int i = this.guiTop + 18;
            int j = i + 112;
            this.currentScroll = ((float)pMouseDragged3 - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
            ((ContainerCurios)this.inventorySlots).scrollTo(this.currentScroll);
            return true;
        } else {
            return super.mouseDragged(pMouseDragged1, pMouseDragged3, pMouseDragged5, pMouseDragged6, pMouseDragged8);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseScrolled1) {
        if (!this.needsScrollBars()) {
            return false;
        } else {
            int i = ((ContainerCurios)this.inventorySlots).curios.map(ICurioItemHandler::getSlots).orElse(1);
            this.currentScroll = (float)((double)this.currentScroll - pMouseScrolled1 / (double)i);
            this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
            ((ContainerCurios)this.inventorySlots).scrollTo(this.currentScroll);
            return true;
        }
    }

    private boolean needsScrollBars() {
        return ((ContainerCurios)this.inventorySlots).canScroll();
    }
}
