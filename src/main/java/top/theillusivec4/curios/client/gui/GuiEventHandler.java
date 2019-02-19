package top.theillusivec4.curios.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
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
            evt.addButton(new GuiButtonCurios(gui,44, gui.getGuiLeft() + 125, gui.height / 2 - 22,
                    20, 18, 50, 0, 19, INVENTORY_BACKGROUND));
        }
    }
}
