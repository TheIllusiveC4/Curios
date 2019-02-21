package top.theillusivec4.curios.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nonnull;

/**
 * {@link LivingCurioChangeEvent} is fired when the Curio of an EntityLivingBase changes. <br>
 * This event is fired whenever changes in curios are detected in {@link net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent}. <br>
 * This also includes entities joining the World, as well as being cloned. <br>
 * This event is fired on server-side only. <br>
 * <br>
 * {@link #type} contains the affected {@link top.theillusivec4.curios.api.CurioType}. <br>
 * {@link #from} contains the {@link ItemStack} that was equipped previously. <br>
 * {@link #to} contains the {@link ItemStack} that is equipped now. <br>
 * {@link #index} contains the index of the curio slot
 * <br>
 * This event is not {@link Cancelable}. <br>
 * <br>
 * This event does not have a result. {@link HasResult} <br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 **/
public class LivingCurioChangeEvent extends LivingEvent {

    private final String type;
    private final ItemStack from;
    private final ItemStack to;
    private final int index;

    public LivingCurioChangeEvent(EntityLivingBase living, String type, int index, @Nonnull ItemStack from, @Nonnull ItemStack to) {
        super(living);
        this.type = type;
        this.from = from;
        this.to = to;
        this.index = index;
    }

    public String getTypeIdentifier() { return this.type; }

    public int getSlotIndex() { return this.index; }

    @Nonnull
    public ItemStack getFrom() { return this.from; }

    @Nonnull
    public ItemStack getTo() { return this.to; }
}
