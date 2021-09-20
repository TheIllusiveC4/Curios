package top.theillusivec4.curios.common.integration.crafttweaker.event;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import org.openzen.zencode.java.ZenCodeType;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.function.Predicate;

@ZenRegister
@Document("mods/Curio/Events/DropRulesEvent")
@NativeTypeRegistration(value = DropRulesEvent.class, zenCodeName = "mods.curios.event.DropRulesEvent")
public class ExpandDropRulesEvent {
    /**
     * @return the DamageSource that caused the drop to occur.
     */
    @ZenCodeType.Getter
    public static DamageSource getDamageSource(DropRulesEvent internal) {
        return internal.getSource();
    }

    /**
     * @return the curio handler for the entity
     */
    @ZenCodeType.Getter("curioHandler")
    public static ICuriosItemHandler getCurioHandler(DropRulesEvent internal) {
        return internal.getCurioHandler();
    }

    /**
     * @return the amount of loot that will be dropped.
     */
    @ZenCodeType.Getter("lootingLevel")
    public static int getLootingLevel(DropRulesEvent internal) {
        return internal.getLootingLevel();
    }

    /**
     * @return whether the Entity doing the drop has recently been damaged.
     */
    @ZenCodeType.Getter
    public static boolean isRecentlyHit(DropRulesEvent internal) {
        return internal.isRecentlyHit();
    }

    /**
     * Adds an override {@link ICurio.DropRule} for the given
     * predicate. Each predicate will be applied to each ItemStack and those that pass will be given
     * the paired DropRule.
     *
     * @param predicate The IItemStack predicate to apply for the DropRule
     * @param dropRule  The DropRule to use as an override. This can be overridden further so there is
     *                  no guarantee for the final result.
     */
    @ZenCodeType.Method
    public static void addOverride(DropRulesEvent internal, Predicate<IItemStack> predicate, ICurio.DropRule dropRule) {
        Predicate<ItemStack> predicateMc = (stack) -> predicate.test(new MCItemStackMutable(stack));
        internal.addOverride(predicateMc, dropRule);
    }
}
