package c4.curios.integration.gamestages;

import c4.curios.integration.gamestages.action.AddSlotSizeStageAction;
import c4.curios.integration.gamestages.action.AddStageAction;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.curios.gamestages")
@ZenRegister
@ModOnly("gamestages")
public class CuriosStagesCrT {

    @ZenMethod
    public static void addSlotSizeStage(String stage, int slotChange, String... identifiers) {
        CraftTweakerAPI.apply(new AddSlotSizeStageAction(stage, slotChange, identifiers));
    }

    @ZenMethod
    public static void addEnableSlotStage(String stage, String... identifier) {
        CraftTweakerAPI.apply(new AddStageAction(stage, identifier));
    }
}
