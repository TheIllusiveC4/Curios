package c4.curios.integration.gamestages.action;

import c4.curios.integration.gamestages.CuriosStages;

public class AddSlotSizeStageAction extends CurioAction {

    private int toShift;

    public AddSlotSizeStageAction(String stage, String identifier, int toShift) {
        super(stage, identifier);
        this.toShift = toShift;
    }

    @Override
    public void apply() {
        CuriosStages.addStageToSlotSize(stage, this.validate().getIdentifier(), toShift);
    }

    @Override
    public String describe() {
        return null;
    }
}
