package c4.curios.client;

import c4.curios.Curios;
import c4.curios.common.network.NetworkHandler;
import c4.curios.common.network.client.CPacketOpenCurios;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class GuiEventHandler {

    public static final ResourceLocation INVENTORY_BACKGROUND = new ResourceLocation(Curios.MODID, "textures/gui/inventory.png");

    @SubscribeEvent
    public void onInventoryGui(GuiScreenEvent.InitGuiEvent.Post evt) {
        if (evt.getGui() instanceof GuiInventory) {
            GuiInventory gui = (GuiInventory)evt.getGui();
            List<GuiButton> buttons = evt.getButtonList();
            buttons.add(new GuiButtonImage(44, gui.getGuiLeft() + 125, gui.height / 2 - 22, 20, 18, 50, 0,
                    19, INVENTORY_BACKGROUND));
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent evt) {
        if (evt.getGui() instanceof GuiInventory) {
            GuiInventory gui = (GuiInventory)evt.getGui();
            if (evt.getButton().id == 44) {
                NetworkHandler.INSTANCE.sendToServer(new CPacketOpenCurios());
            } else if (evt.getButton().id == 10) {
                for (GuiButton button : evt.getButtonList()) {
                    if (button.id == 44 && button instanceof GuiButtonImage) {
                        ((GuiButtonImage) button).setPosition(gui.getGuiLeft() + 125, gui.height / 2 - 22);
                    }
                }
            }
        }
    }
}
