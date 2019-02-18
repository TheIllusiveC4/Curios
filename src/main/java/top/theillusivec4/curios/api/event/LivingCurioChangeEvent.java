package top.theillusivec4.curios.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import top.theillusivec4.curios.api.CurioType;

import javax.annotation.Nonnull;

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
