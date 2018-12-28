package c4.curios.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;

import javax.annotation.Nonnull;

public class LivingChangeCurioEvent extends LivingEvent {

    private final String slot;
    private final ItemStack from;
    private final ItemStack to;

    public LivingChangeCurioEvent(EntityLivingBase living, String slot, @Nonnull ItemStack from, @Nonnull ItemStack to)
    {
        super(living);
        this.slot = slot;
        this.from = from;
        this.to = to;
    }

    public String getSlot() { return this.slot; }
    @Nonnull
    public ItemStack getFrom() { return this.from; }
    @Nonnull
    public ItemStack getTo() { return this.to; }
}
