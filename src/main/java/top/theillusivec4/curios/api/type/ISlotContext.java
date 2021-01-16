package top.theillusivec4.curios.api.type;

import net.minecraft.entity.LivingEntity;

public interface ISlotContext {

  String getIdentifier();

  int getIndex();

  LivingEntity getWearer();
}
