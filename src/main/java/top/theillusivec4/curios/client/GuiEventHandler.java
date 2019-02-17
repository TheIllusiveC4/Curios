package top.theillusivec4.curios.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;

import java.util.List;

public class GuiEventHandler {

    public static final ResourceLocation INVENTORY_BACKGROUND = new ResourceLocation(Curios.MODID, "textures/gui/inventory.png");

    @SubscribeEvent
    public void onInventoryGui(GuiScreenEvent.InitGuiEvent.Post evt) {
        if (evt.getGui() instanceof GuiInventory) {
            GuiInventory gui = (GuiInventory)evt.getGui();
            List<GuiButton> buttons = evt.getButtonList();
            buttons.add(new GuiButtonImage(44, gui.getGuiLeft() + 125, gui.height / 2 - 22, 20, 18, 50, 0,
                    19, INVENTORY_BACKGROUND) {

                @Override
                public void onClick(double mouseX, double mouseY) {
                    NetworkHandler.INSTANCE.sendToServer(new CPacketOpenCurios());
                }
            });
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent evt) {

        if (evt.getGui() instanceof GuiInventory) {
            GuiInventory gui = (GuiInventory)evt.getGui();

            if (evt.getButton().id == 10) {

                for (GuiButton button : evt.getButtonList()) {

                    if (button.id == 44 && button instanceof GuiButtonImage) {
                        ((GuiButtonImage) button).setPosition(gui.getGuiLeft() + 125, gui.height / 2 - 22);
                    }
                }
            }
        }
    }
}
