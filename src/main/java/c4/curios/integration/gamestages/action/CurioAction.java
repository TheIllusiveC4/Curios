package c4.curios.integration.gamestages.action;

import c4.curios.api.CuriosAPI;
import c4.curios.api.inventory.CurioSlotEntry;
import crafttweaker.IAction;

import javax.annotation.Nonnull;

public abstract class CurioAction implements IAction {

    protected final String stage;
    protected final String identifier;

    public CurioAction(String stage, String identifier) {
        this.stage = stage;
        this.identifier = identifier;
    }

    @Nonnull
    public CurioSlotEntry validate() {

        if (stage.isEmpty()) {
            throw new IllegalArgumentException("Empty stage name for this entry");
        }

        CurioSlotEntry info = CuriosAPI.getSlotEntryForID(identifier);

        if (info == null) {
            throw new IllegalArgumentException("No Curio slot found for identifier " + identifier);
        }

        return info;
    }
}
