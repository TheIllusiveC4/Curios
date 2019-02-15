package c4.curios.integration.gamestages;

import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurioItemHandler;
import c4.curios.common.inventory.ContainerCurios;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.Set;

public class CuriosStages {

    private static final Map<String, Set<String>> SLOT_STAGES = Maps.newHashMap();
    private static final Map<String, Map<Integer, Set<String>>> SLOT_CHANGE_STAGES = Maps.newHashMap();

    @SubscribeEvent
    public void onAddedStage(GameStageEvent.Added evt) {
        EntityPlayer player = evt.getEntityPlayer();
        ICurioItemHandler handler = CuriosAPI.getCuriosHandler(player);

        if (handler != null) {
            if (player.openContainer instanceof ContainerCurios) {
                player.closeScreen();
            }
            String stage = evt.getStageName();

            if (SLOT_STAGES.containsKey(stage)) {
                Set<String> slotIds = SLOT_STAGES.get(stage);

                for (String id : slotIds) {
                    CuriosAPI.enableSlotForEntity(id, player);
                }
            }

            if (SLOT_CHANGE_STAGES.containsKey(stage)) {
                Map<Integer, Set<String>> changeMap = SLOT_CHANGE_STAGES.get(stage);

                for (Integer shift : changeMap.keySet()) {

                    for (String id : changeMap.get(shift)) {
                        CuriosAPI.addSlotsToEntity(id, shift, player);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRemovedStage(GameStageEvent.Removed evt) {
        EntityPlayer player = evt.getEntityPlayer();
        ICurioItemHandler handler = CuriosAPI.getCuriosHandler(player);

        if (handler != null) {
            if (player.openContainer instanceof ContainerCurios) {
                player.closeScreen();
            }
            String stage = evt.getStageName();

            if (SLOT_STAGES.containsKey(stage)) {
                Set<String> slotIds = SLOT_STAGES.get(stage);

                for (String id : slotIds) {
                    CuriosAPI.disableSlotForEntity(id, player);
                }
            }

            if (SLOT_CHANGE_STAGES.containsKey(stage)) {
                Map<Integer, Set<String>> changeMap = SLOT_CHANGE_STAGES.get(stage);

                for (Integer shift : changeMap.keySet()) {

                    for (String id : changeMap.get(shift)) {
                        CuriosAPI.addSlotsToEntity(id, -shift, player);
                    }
                }
            }
        }
    }

    public static void addStageToSlot(String stage, String identifier) {
        SLOT_STAGES.merge(identifier, Sets.newHashSet(stage), (k, v) -> {
            v.add(stage);
            return v;
        });
    }


    public static void addStageToSlotSize(String stage, String identifier, int slotChange) {
        Map<Integer, Set<String>> entries = SLOT_CHANGE_STAGES.getOrDefault(stage, Maps.newHashMap());
        Set<String> slotIds = entries.get(slotChange);

        if (slotIds != null) {
            slotIds.add(identifier);
        } else {
            entries.put(slotChange, Sets.newHashSet(identifier));
        }
        SLOT_CHANGE_STAGES.putIfAbsent(stage, entries);
    }
}
