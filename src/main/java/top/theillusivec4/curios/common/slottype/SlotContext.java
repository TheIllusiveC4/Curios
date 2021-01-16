package top.theillusivec4.curios.common.slottype;

import net.minecraft.entity.LivingEntity;
import top.theillusivec4.curios.api.type.ISlotContext;

public class SlotContext implements ISlotContext {

  final String id;
  final int index;
  final LivingEntity wearer;

  public SlotContext(String id, int index, LivingEntity livingEntity) {
    this.id = id;
    this.index = index;
    this.wearer = livingEntity;
  }

  @Override
  public String getIdentifier() {
    return id;
  }

  @Override
  public int getIndex() {
    return index;
  }

  @Override
  public LivingEntity getWearer() {
    return wearer;
  }
}
