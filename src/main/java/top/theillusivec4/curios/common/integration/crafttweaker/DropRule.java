package top.theillusivec4.curios.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import net.minecraft.entity.LivingEntity;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * Used by {@link ICurio#getDropRule(LivingEntity)} to determine drop on death behavior.
 * <br>
 * DEFAULT - normal vanilla behavior with drops dictated by the Keep Inventory game rule
 * <br>
 * ALWAYS_DROP - always drop regardless of game rules
 * <br>
 * ALWAYS_KEEP - always keep regardless of game rules
 * <br>
 * DESTROY - destroy the item upon death
 */
@ZenRegister
@Document("mods/Curio/DropRule")
@NativeTypeRegistration(value = ICurio.DropRule.class, zenCodeName = "mods.curio.DropRule")
public class DropRule {
}
