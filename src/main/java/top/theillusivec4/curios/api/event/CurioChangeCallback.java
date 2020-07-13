package top.theillusivec4.curios.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface CurioChangeCallback {

  Event<CurioChangeCallback> EVENT = EventFactory.createArrayBacked(CurioChangeCallback.class,
      (listeners) -> (living, id, index, from, to) -> {
        for (CurioChangeCallback listener : listeners) {
          listener.onChange(living, id, index, from, to);
        }
      });

  void onChange(LivingEntity livingEntity, String id, int index, ItemStack from, ItemStack to);
}
