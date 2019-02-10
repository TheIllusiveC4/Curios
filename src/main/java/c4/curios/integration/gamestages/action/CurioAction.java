package c4.curios.integration.gamestages.action;

import c4.curios.api.CuriosAPI;
import c4.curios.api.inventory.CurioSlotEntry;
import com.google.common.collect.Lists;
import crafttweaker.IAction;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class CurioAction implements IAction {

    protected final String stage;
    protected final String[] identifiers;

    public CurioAction(String stage, String... identifiers) {
        this.stage = stage;
        this.identifiers = identifiers;
    }

    @Nonnull
    public List<CurioSlotEntry> validate() {

        if (stage.isEmpty()) {
            throw new IllegalArgumentException("Empty stage name for this entry");
        }
        List<CurioSlotEntry> entries = Lists.newArrayList();

        for (String id : identifiers) {
            CurioSlotEntry info = CuriosAPI.getRegistry().get(id);

            if (info == null) {
                throw new IllegalArgumentException("No Curio slot found for identifier " + id);
            }
            entries.add(info);
        }
        return entries;
    }
}
