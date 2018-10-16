package c4.curios.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

import javax.annotation.Nonnull;

public class PlayerCurioChangeEvent extends PlayerEvent {

    private final String slot;
    private final ItemStack from;
    private final ItemStack to;

    public PlayerCurioChangeEvent(EntityPlayer player, String slot, @Nonnull ItemStack from, @Nonnull ItemStack to)
    {
        super(player);
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
