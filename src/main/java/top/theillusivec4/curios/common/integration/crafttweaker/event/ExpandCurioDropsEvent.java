package top.theillusivec4.curios.common.integration.crafttweaker.event;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.DamageSource;
import org.openzen.zencode.java.ZenCodeType;
import top.theillusivec4.curios.api.event.CurioDropsEvent;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.List;

/**
 * LivingCurioDropsEvent is fired when an Entity's death causes dropped curios to appear.<br> This
 * event is fired whenever an Entity dies and drops items in {@link LivingEntity#onDeath(DamageSource)}.<br>
 * <br>
 * This event is fired inside the {@link net.minecraftforge.event.entity.living.LivingDropsEvent}.<br>
 *
 * @docEvent canceled the Entity does not drop anything.
 **/
@ZenRegister
@Document("mods/Curios/Events/CurioDropsEvent")
@NativeTypeRegistration(value = CurioDropsEvent.class, zenCodeName = "mods.curios.event.CurioDropsEvent")
public class ExpandCurioDropsEvent {
    /**
     * the DamageSource that caused the drop to occur.
     */
    @ZenCodeType.Getter("damageSource")
    public static DamageSource getDamageSource(CurioDropsEvent internal) {
        return internal.getSource();
    }

    /**
     * the List of ItemEntity that will be dropped.
     */
    @ZenCodeType.Getter("drops")
    public static List<ItemEntity> getDrops(CurioDropsEvent internal) {
        return ((List<ItemEntity>) internal.getDrops());
    }

    /**
     * the curio handler for the entity
     */
    @ZenCodeType.Getter("curioHandler")
    public static ICuriosItemHandler getCurioHandler(CurioDropsEvent internal) {
        return internal.getCurioHandler();
    }

    /**
     * the amount of loot that will be dropped.
     */
    @ZenCodeType.Getter("lootingLevel")
    public static int getLootingLevel(CurioDropsEvent internal) {
        return internal.getLootingLevel();
    }

    /**
     * whether the Entity doing the drop has recently been damaged.
     */
    @ZenCodeType.Getter("isRecentlyHit")
    public static boolean isRecentlyHit(CurioDropsEvent internal) {
        return internal.isRecentlyHit();
    }
}
