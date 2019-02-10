package c4.curios.integration.gamestages.action;

import c4.curios.api.inventory.CurioSlotEntry;
import c4.curios.integration.gamestages.CuriosStages;

public class AddStageAction extends CurioAction {

    public AddStageAction(String stage, String... identifier) {
        super(stage, identifier);
    }

    @Override
    public void apply() {

        for (CurioSlotEntry entry : this.validate()) {
            CuriosStages.addStageToSlot(stage, entry.getIdentifier());
        }
    }

    @Override
    public String describe() {
        return null;
    }
}
