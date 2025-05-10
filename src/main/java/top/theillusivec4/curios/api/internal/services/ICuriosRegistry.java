package top.theillusivec4.curios.api.internal.services;

import net.minecraft.core.component.DataComponentType;
import top.theillusivec4.curios.api.CurioAttributeModifiers;

public interface ICuriosRegistry {

  DataComponentType<CurioAttributeModifiers> getAttributeModifierComponent();
}
