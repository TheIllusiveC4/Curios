package top.theillusivec4.curios.api.event;

import java.util.Collection;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.type.component.ICuriosItemHandler;

public interface CurioDropsCallback {

  Event<CurioDropsCallback> EVENT = EventFactory.createArrayBacked(CurioDropsCallback.class,
      (listeners) -> (living, handler, source, drops, lootingLevel, recentlyHit) -> {
        for (CurioDropsCallback listener : listeners) {

          if (!listener.drop(living, handler, source, drops, lootingLevel, recentlyHit)) {
            return false;
          }
        }
        return true;
      });

  boolean drop(LivingEntity livingEntity, ICuriosItemHandler handler, DamageSource source,
      Collection<ItemStack> drops, int lootingLevel, boolean recentlyHit);
}
