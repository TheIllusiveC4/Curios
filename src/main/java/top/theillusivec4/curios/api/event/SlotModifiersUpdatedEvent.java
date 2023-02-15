package top.theillusivec4.curios.api.event;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * {@link SlotModifiersUpdatedEvent} is fired when the slot size is dynamically changed during
 * gameplay through slot modifiers.
 * <br> This event is fired on both the client and the server.
 * <br>
 * {@link #types} contains the affected {@link top.theillusivec4.curios.api.type.ISlotType}. <br>
 * <br>
 * This event is not {@link Cancelable}. <br>
 * <br>
 * This event does not have a result. {@link HasResult} <br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 **/
public class SlotModifiersUpdatedEvent extends LivingEvent {

  private final Set<String> types;

  public SlotModifiersUpdatedEvent(LivingEntity livingEntity, Set<String> types) {
    super(livingEntity);
    this.types = types;
  }

  public Set<String> getTypes() {
    return ImmutableSet.copyOf(this.types);
  }
}