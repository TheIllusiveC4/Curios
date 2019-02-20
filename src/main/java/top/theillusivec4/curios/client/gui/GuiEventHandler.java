package top.theillusivec4.curios.client.gui;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuiEventHandler {

    @SubscribeEvent
    public void onInventoryGui(GuiScreenEvent.InitGuiEvent.Post evt) {

        if (evt.getGui() instanceof GuiInventory) {
            GuiInventory gui = (GuiInventory)evt.getGui();
            evt.addButton(new GuiButtonCurios(gui,44, gui.getGuiLeft() + 125, gui.height / 2 - 22,
                    20, 18, 50, 0, 19, GuiContainerCurios.CURIO_INVENTORY));
        }
    }
}