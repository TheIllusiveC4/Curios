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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

/**
 * LivingCurioDropsEvent is fired when an Entity's death causes dropped curios to appear.<br> This
 * event is fired whenever an Entity dies and drops items in {@link LivingEntity#die(DamageSource)}.<br>
 * <br>
 * This event is fired inside the {@link net.minecraftforge.event.entity.living.LivingDropsEvent}.<br>
 * <br>
 * {@link #source} contains the DamageSource that caused the drop to occur.<br> {@link #drops}
 * contains the ArrayList of ItemEntity that will be dropped.<br> {@link #lootingLevel} contains the
 * amount of loot that will be dropped.<br> {@link #recentlyHit} determines whether the Entity doing
 * the drop has recently been damaged.<br>
 * <br>
 * This event is {@link Cancelable}.<br> If this event is canceled, the Entity does not drop
 * anything.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 **/
@Cancelable
public class CurioDropsEvent extends LivingEvent {

  private final DamageSource source;
  private final Collection<ItemEntity> drops;
  private final int lootingLevel;
  private final boolean recentlyHit;
  private final ICuriosItemHandler curioHandler; // Curio handler for the entity

  public CurioDropsEvent(LivingEntity entity, ICuriosItemHandler handler, DamageSource source,
                         Collection<ItemEntity> drops, int lootingLevel, boolean recentlyHit) {
    super(entity);
    this.source = source;
    this.drops = drops;
    this.lootingLevel = lootingLevel;
    this.recentlyHit = recentlyHit;
    this.curioHandler = handler;
  }

  public ICuriosItemHandler getCurioHandler() {
    return curioHandler;
  }

  public DamageSource getSource() {
    return source;
  }

  public Collection<ItemEntity> getDrops() {
    return drops;
  }

  public int getLootingLevel() {
    return lootingLevel;
  }

  public boolean isRecentlyHit() {
    return recentlyHit;
  }
}
