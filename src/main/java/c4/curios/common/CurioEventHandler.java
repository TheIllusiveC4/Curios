package c4.curios.common;

import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurio;
import c4.curios.api.capability.ICurioItemHandler;
import c4.curios.api.inventory.CurioSlot;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;
import java.util.UUID;

public class CurioEventHandler {

    private static final Map<UUID, NonNullList<CurioSlot>> curioMap = Maps.newHashMap();

    @SubscribeEvent
    public void onCurioTick(TickEvent.PlayerTickEvent evt) {

        if (evt.phase == TickEvent.Phase.END) {
            EntityPlayer player = evt.player;
            ICurioItemHandler curioHandler = CuriosAPI.getCuriosHandler(player);

            if (curioHandler != null) {
                NonNullList<CurioSlot> curios = curioHandler.getCurioStacks();

                for (int i = 0; i < curios.size(); i++) {
                    ItemStack stack = curioHandler.getStackInSlot(i);
                    ICurio curio = CuriosAPI.getCurio(stack);

                    if (curio != null) {
                        curio.onCurioTick(stack, player);
                    }
                }
            }
        }
    }
}
