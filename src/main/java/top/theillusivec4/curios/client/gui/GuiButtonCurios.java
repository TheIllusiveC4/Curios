package top.theillusivec4.curios.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;
import top.theillusivec4.curios.common.network.client.CPacketOpenVanilla;

public class GuiButtonCurios extends GuiButtonImage {

    private final GuiContainer parentGui;
    private boolean isRecipeBookVisible = false;

    GuiButtonCurios(GuiContainer parentGui, int buttonId, int xIn, int yIn, int widthIn, int heightIn, int textureOffestX,
                          int textureOffestY, int yDiffText, ResourceLocation resource) {
        super(buttonId, xIn, yIn, widthIn, heightIn, textureOffestX, textureOffestY, yDiffText, resource);
        this.parentGui = parentGui;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        if (parentGui instanceof GuiContainerCurios) {
            GuiInventory inventory = new GuiInventory(mc.player);
            ObfuscationReflectionHelper.setPrivateValue(GuiInventory.class, inventory, (float)mouseX, "field_147048_u");
            ObfuscationReflectionHelper.setPrivateValue(GuiInventory.class, inventory, (float)mouseY, "field_147047_v");
            mc.displayGuiScreen(inventory);
            NetworkHandler.INSTANCE.sendToServer(new CPacketOpenVanilla());
        } else {
            NetworkHandler.INSTANCE.sendToServer(new CPacketOpenCurios());
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {

        if (parentGui instanceof GuiInventory) {
            boolean lastVisible = isRecipeBookVisible;
            IGuiEventListener eventListener = parentGui.getFocused();

            if (eventListener != null) {
                isRecipeBookVisible = ((GuiRecipeBook)eventListener).isVisible();
            }

            if (lastVisible != isRecipeBookVisible) {
                this.setPosition(parentGui.getGuiLeft() + 125, parentGui.height / 2 - 22);
            }
        }
        super.render(mouseX, mouseY, partialTicks);
    }
}
