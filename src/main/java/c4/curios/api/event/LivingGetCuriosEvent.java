package c4.curios.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class LivingGetCuriosEvent extends LivingEvent {

    private final String identifier;
    private int size;

    public LivingGetCuriosEvent(EntityLivingBase living, String identifier, int size) {
        super(living);
        this.identifier = identifier;
        this.size = size;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
