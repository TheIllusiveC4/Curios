package c4.curios.client;

import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurio;
import c4.curios.common.network.CPacketOpenCurios;
import c4.curios.common.network.NetworkHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

public class ClientEventHandler {

    @SubscribeEvent
    public void onKeyInput(TickEvent.ClientTickEvent evt) {

        if (evt.phase != TickEvent.Phase.END) return;

        if (KeyRegistry.openCurios.isPressed()) {
            NetworkHandler.INSTANCE.sendToServer(new CPacketOpenCurios());
        }
    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent evt) {
        if (!evt.getItemStack().isEmpty()) {
            ICurio curio = CuriosAPI.getCurio(evt.getItemStack());
            if (curio != null) {
                List<String> tooltip = evt.getToolTip();
                tooltip.add(TextFormatting.AQUA + I18n.format("curios.name"));
                List<String> slots = curio.getCurioSlots(evt.getItemStack());
                if (slots.isEmpty()) {
                    tooltip.add(" -" + I18n.format("curios.identifier.generic"));
                } else {
                    for (String s : curio.getCurioSlots(evt.getItemStack())) {
                        tooltip.add(" -" + I18n.format("curios.identifier." + s));
                    }
                }
            }
        }
    }
}
