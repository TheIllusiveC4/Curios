package top.theillusivec4.curios.api;

import net.minecraft.world.entity.LivingEntity;

public final class SlotContext {

  // The identifier of the slot
  final String id;
  // The index of the slot within its slot type
  final int index;
  // The owner of the slot
  final LivingEntity wearer;

  public SlotContext() {
    this("", null, -1);
  }

  public SlotContext(String id) {
    this(id, null, -1);
  }

  public SlotContext(String id, LivingEntity wearer) {
    this(id, wearer, -1);
  }

  public SlotContext(String id, LivingEntity wearer, int index) {
    this.id = id;
    this.index = index;
    this.wearer = wearer;
  }

  public String getIdentifier() {
    return id;
  }

  public int getIndex() {
    return index;
  }

  public LivingEntity getWearer() {
    return wearer;
  }
}
