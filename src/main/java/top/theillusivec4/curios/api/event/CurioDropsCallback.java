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
