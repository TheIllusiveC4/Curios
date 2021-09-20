package top.theillusivec4.curios.common.integration.contenttweaker;

import com.blamejared.contenttweaker.api.functions.ICotFunction;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.entity.LivingEntity;
import org.openzen.zencode.java.ZenCodeType;
import top.theillusivec4.curios.api.type.capability.ICurio;

@ZenRegister(modDeps = "contenttweaker")
@FunctionalInterface
@Document("mods/Curios/ContentTweaker/IDropRuleFunction")
@ZenCodeType.Name("mods.curios.contenttweaker.IDropRuleFunction")
public interface IDropRuleFunction extends ICotFunction {
    @ZenCodeType.Method
    ICurio.DropRule call(LivingEntity entity);
}
