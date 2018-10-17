package c4.curios.integration.gamestages.action;

import c4.curios.integration.gamestages.CuriosStages;

public class RemoveStageAction extends CurioAction {

    public RemoveStageAction(String stage, String identifier) {
        super(stage, identifier);
    }

    @Override
    public void apply() {
        CuriosStages.removeStageFromSlot(stage, this.validate().getIdentifier());
    }

    @Override
    public String describe() {
        return null;
    }
}
