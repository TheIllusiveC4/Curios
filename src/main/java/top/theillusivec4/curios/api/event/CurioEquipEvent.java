package top.theillusivec4.curios.api.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event;
import top.theillusivec4.curios.api.SlotContext;

/**
 * CurioEquipEvent is fired when a curio item is about to be equipped and allows an event listener
 * to specify whether it should or not. <br>
 * This event is fired when ever the {@link top.theillusivec4.curios.api.type.capability.ICurio#canEquip(SlotContext)}
 * is checked. <br>
 * <br>
 * This event has a {@link HasResult result}:
 * <li>{@link Result#ALLOW} means the curio item can be equipped.</li>
 * <li>{@link Result#DEFAULT} means the item tags and {@link top.theillusivec4.curios.api.type.capability.ICurio#canEquip(SlotContext)}
 * determines the result.</li>
 * <li>{@link Result#DENY} means the curio item cannot be equipped.</li><br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.
 */
@Event.HasResult
public class CurioEquipEvent extends LivingEvent {

  private final SlotContext slotContext;
  private final ItemStack stack;

  public CurioEquipEvent(ItemStack stack, SlotContext slotContext) {
    super(slotContext.entity());
    this.slotContext = slotContext;
    this.stack = stack;
  }

  public SlotContext getSlotContext() {
    return slotContext;
  }

  public ItemStack getStack() {
    return stack;
  }
}
