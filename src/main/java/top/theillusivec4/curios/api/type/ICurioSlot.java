package top.theillusivec4.curios.api.type;

import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.extensions.ICurioSlotExtension;

/**
 * Representation of a curio slot in menus and user interfaces
 */
public interface ICurioSlot {

  default String getId() {
    return this.getSlotContext().identifier();
  }

  ICurioSlotExtension getSlotExtension();

  SlotContext getSlotContext();
}
