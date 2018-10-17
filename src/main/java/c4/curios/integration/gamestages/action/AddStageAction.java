package c4.curios.integration.gamestages.action;

import c4.curios.integration.gamestages.CuriosStages;

public class AddStageAction extends CurioAction {

    public AddStageAction(String stage, String identifier) {
        super(stage, identifier);
    }

    @Override
    public void apply() {
        CuriosStages.addStageToSlot(stage, this.validate().getIdentifier());
    }

    @Override
    public String describe() {
        return null;
    }
}
