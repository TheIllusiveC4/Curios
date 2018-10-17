package c4.curios.integration.gamestages;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;

public class CuriosStages {

    private static final Map<String, Set<String>> slotToStages = Maps.newHashMap();

    public static Set<String> getStages(String identifier) {
        return ImmutableSet.copyOf(slotToStages.getOrDefault(identifier, new HashSet<>()));
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
