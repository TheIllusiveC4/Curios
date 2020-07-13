package top.theillusivec4.curios.api.event;

import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import top.theillusivec4.curios.api.type.component.ICurio.DropRule;
import top.theillusivec4.curios.api.type.component.ICuriosItemHandler;

public interface DropRulesCallback {

  Event<DropRulesCallback> EVENT = EventFactory.createArrayBacked(DropRulesCallback.class,
      (listeners) -> (living, handler, source, drops, lootingLevel, recentlyHit) -> {
        for (DropRulesCallback listener : listeners) {
          listener.dropRules(living, handler, source, drops, lootingLevel, recentlyHit);
        }
      });

  void dropRules(LivingEntity livingEntity, ICuriosItemHandler handler, DamageSource source,
      int lootingLevel, boolean recentlyHit, List<Pair<Predicate<ItemStack>, DropRule>> overrides);
}
