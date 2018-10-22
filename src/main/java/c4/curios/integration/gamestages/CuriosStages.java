package c4.curios.integration.gamestages;

import c4.curios.api.event.LivingCollectCuriosEvent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.*;

public class CuriosStages {

    private static final Map<String, Set<String>> slotToStages = Maps.newHashMap();

    @SubscribeEvent
    public void onCurioCollection(LivingCollectCuriosEvent evt) {

        if (evt.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)evt.getEntityLiving();
            Set<String> identifiers = evt.getIds();

            for (Iterator<String> i = identifiers.iterator(); i.hasNext();) {
                String id = i.next();

                if (slotToStages.containsKey(id)) {
                    Set<String> stages = getStages(id);

                    if (stages != null && !GameStageHelper.hasAllOf(player, stages)) {
                        i.remove();
                    }
                }
            }
        }
    }

    @Nullable
    public static Set<String> getStages(String identifier) {
        Set<String> stages = slotToStages.get(identifier);

        if (stages != null) {
            return ImmutableSet.copyOf(stages);
        } else {
            return null;
        }
    }

    public static void addStageToSlot(String stage, String identifier) {
        slotToStages.merge(identifier, Sets.newHashSet(stage), (k, v) -> {
            v.add(stage);
            return v;
        });
    }

    public static void removeStageFromSlot(String stage, String identifier) {
        slotToStages.computeIfPresent(identifier, (k, v) -> {
            v.remove(stage);
            return v.isEmpty() ? null : v;
        });
    }
}
