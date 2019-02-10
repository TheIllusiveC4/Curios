package c4.curios.integration.gamestages.action;

import c4.curios.api.inventory.CurioSlotEntry;
import c4.curios.integration.gamestages.CuriosStages;

public class AddSlotSizeStageAction extends CurioAction {

    private int toShift;

    public AddSlotSizeStageAction(String stage, int toShift, String... identifiers) {
        super(stage, identifiers);
        this.toShift = toShift;
    }

    @Override
    public void apply() {

        for (CurioSlotEntry entry : this.validate()) {
            CuriosStages.addStageToSlotSize(stage, entry.getIdentifier(), toShift);
        }
    }

    @Override
    public String describe() {
        return null;
    }
}
