package c4.curios.integration.gamestages;

import c4.curios.integration.gamestages.action.AddStageAction;
import c4.curios.integration.gamestages.action.RemoveStageAction;
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
    public static void addStage(String stage, String identifier) {
        CraftTweakerAPI.apply(new AddStageAction(stage, identifier));
    }

    @ZenMethod
    public static void removeStage(String stage, String identifier) {
        CraftTweakerAPI.apply(new RemoveStageAction(stage, identifier));
    }

}
