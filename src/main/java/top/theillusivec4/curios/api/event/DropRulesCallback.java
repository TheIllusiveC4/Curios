/*
 * Copyright (c) 2018-2020 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

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
