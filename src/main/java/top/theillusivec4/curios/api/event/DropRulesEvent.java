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

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurio.DropRule;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

/**
 * LivingCurioDropsEvent is fired when an Entity's death causes dropped curios to appear.<br> This
 * event is fired whenever an Entity dies and drops items in {@link LivingEntity#die(DamageSource)}.<br>
 * <br>
 * This event is fired inside the {@link net.minecraftforge.event.entity.living.LivingDropsEvent}.<br>
 * <br>
 * {@link #source} contains the DamageSource that caused the drop to occur.<br>  {@link
 * #lootingLevel} contains the amount of loot that will be dropped.<br> {@link #recentlyHit}
 * determines whether the Entity doing the drop has recently been damaged.<br>
 * <br>
 * This event is not {@link Cancelable}.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 **/
public class DropRulesEvent extends LivingEvent {

  private final DamageSource source;
  private final int lootingLevel;
  private final boolean recentlyHit;
  private final ICuriosItemHandler curioHandler; // Curio handler for the entity
  private final List<Tuple<Predicate<ItemStack>, DropRule>> overrides = new ArrayList<>();
      // List of drop rule overrides

  public DropRulesEvent(LivingEntity entity, ICuriosItemHandler handler, DamageSource source,
                        int lootingLevel, boolean recentlyHit) {
    super(entity);
    this.source = source;
    this.lootingLevel = lootingLevel;
    this.recentlyHit = recentlyHit;
    this.curioHandler = handler;
  }

  public DamageSource getSource() {
    return source;
  }

  public int getLootingLevel() {
    return lootingLevel;
  }

  public boolean isRecentlyHit() {
    return recentlyHit;
  }

  public ICuriosItemHandler getCurioHandler() {
    return curioHandler;
  }

  /**
   * Adds an override {@link ICurio.DropRule} for the given
   * predicate. Each predicate will be applied to each ItemStack and those that pass will be given
   * the paired DropRule.
   *
   * @param predicate The ItemStack predicate to apply for the DropRule
   * @param dropRule  The DropRule to use as an override. This can be overridden further so there is
   *                  no guarantee for the final result.
   */
  public void addOverride(Predicate<ItemStack> predicate, DropRule dropRule) {
    overrides.add(new Tuple<>(predicate, dropRule));
  }

  public ImmutableList<Tuple<Predicate<ItemStack>, DropRule>> getOverrides() {
    return ImmutableList.copyOf(overrides);
  }
}
