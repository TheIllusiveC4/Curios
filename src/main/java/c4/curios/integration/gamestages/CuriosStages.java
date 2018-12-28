package c4.curios.integration.gamestages;

import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurioItemHandler;
import c4.curios.api.event.LivingGetCuriosEvent;
import c4.curios.common.inventory.ContainerCurios;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CuriosStages {

    private static final Map<String, Set<String>> SLOT_STAGES = Maps.newHashMap();
    private static final Map<String, List<Tuple<String, Integer>>> SLOT_CHANGE_STAGES = Maps.newHashMap();

    @SubscribeEvent
    public void onGetCurio(LivingGetCuriosEvent evt) {

        if (evt.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)evt.getEntityLiving();
            String identifier = evt.getIdentifier();

            if (SLOT_STAGES.containsKey(identifier)) {
                Set<String> stages = getStages(identifier);

                if (stages != null) {

                    if (!GameStageHelper.hasAllOf(player, stages)) {
                        evt.setCanceled(true);
                    } else if (SLOT_CHANGE_STAGES.containsKey(identifier)) {
                        List<Tuple<String, Integer>> list = SLOT_CHANGE_STAGES.get(identifier);

                        for (Tuple<String, Integer> entry : list) {

                            if (GameStageHelper.hasStage(player, entry.getFirst())) {
                                evt.setSize(evt.getSize() + entry.getSecond());
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onAddedStage(GameStageEvent.Added evt) {
        EntityPlayer player = evt.getEntityPlayer();
        ICurioItemHandler handler = CuriosAPI.getCuriosHandler(player);

        if (handler != null) {
            if (player.openContainer instanceof ContainerCurios) {
                player.closeScreen();
            }
        }
    }

    @Nullable
    public static Set<String> getStages(String identifier) {
        Set<String> stages = SLOT_STAGES.get(identifier);

        if (stages != null) {
            return ImmutableSet.copyOf(stages);
        } else {
            return null;
        }
    }

    public static void addStageToSlot(String stage, String identifier) {
        SLOT_STAGES.merge(identifier, Sets.newHashSet(stage), (k, v) -> {
            v.add(stage);
            return v;
        });
    }


    public static void addStageToSlotSize(String stage, String identifier, int slotChange) {
        List<Tuple<String, Integer>> list = SLOT_CHANGE_STAGES.getOrDefault(identifier, Lists.newArrayList(new Tuple<>(stage, slotChange)));
        SLOT_CHANGE_STAGES.put(identifier, list);
    }
}
