package top.theillusivec4.curios.common.integration.crafttweaker.event;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStack;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import net.minecraft.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

/**
 * {@link CurioChangeEvent} is fired when the Curio of a LivingEntity changes. <br> This event is
 * fired whenever changes in curios are detected in
 * <br> {@link net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent}.
 * <br> This also includes entities joining the World, as well as being cloned. <br> This event is
 * fired on server-side only.
 **/
@Document("mods/Curios/Events/CurioChangeEvent")
@ZenRegister
@NativeTypeRegistration(value = CurioChangeEvent.class, zenCodeName = "mods.curios.event.CurioChangeEvent")
public class ExpandCurioChangeEvent {
    /**
     * @return the affected {@link top.theillusivec4.curios.api.type.ISlotType}
     */
    @ZenCodeType.Getter("identifier")
    public static String getIdentifier(CurioChangeEvent internal) {
        return internal.getIdentifier();
    }

    /**
     * @return the {@link IItemStack} that was equipped previously.
     */
    @ZenCodeType.Getter("from")
    public static IItemStack getFrom(CurioChangeEvent internal) {
        return new MCItemStack(internal.getFrom());
    }

    /**
     * @return the {@link ItemStack} that is equipped now.
     */
    @ZenCodeType.Getter("to")
    public static IItemStack getTo(CurioChangeEvent internal) {
        return new MCItemStack(internal.getTo());
    }

    /**
     * @return index of the curio slot.
     */
    @ZenCodeType.Getter("slotIndex")
    public static int getSlotIndex(CurioChangeEvent internal) {
        return internal.getSlotIndex();
    }
}
