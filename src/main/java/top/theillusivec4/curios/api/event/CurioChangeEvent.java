/*
 * Copyright (c) 2018-2024 C4
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
 *
 */

package top.theillusivec4.curios.api.event;

import javax.annotation.Nonnull;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * {@link CurioChangeEvent} is fired when the curio item of a LivingEntity changes.
 *
 * <p>This event is fired whenever changes in curios are detected in
 * {@link net.neoforged.neoforge.event.tick.EntityTickEvent}.
 *
 * <p>This also includes entities joining the World, as well as being cloned.
 *
 * <p>This event is fired on server-side only.
 *
 * <br>{@link #type} contains the affected {@link top.theillusivec4.curios.api.type.ISlotType}.
 * <br>{@link #from} contains the {@link ItemStack} that was equipped previously.
 * <br>{@link #to} contains the {@link ItemStack} that is equipped now.
 * <br>{@link #index} contains the index of the curio slot
 *
 * <p>This event is fired on the {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS}.
 **/
public abstract class CurioChangeEvent extends LivingEvent {

  private final String type;
  private final ItemStack from;
  private final ItemStack to;
  private final int index;

  public CurioChangeEvent(LivingEntity living, String type, int index, @Nonnull ItemStack from,
                          @Nonnull ItemStack to) {
    super(living);
    this.type = type;
    this.from = from;
    this.to = to;
    this.index = index;
  }

  public String getIdentifier() {
    return this.type;
  }

  public int getSlotIndex() {
    return this.index;
  }

  @Nonnull
  public ItemStack getFrom() {
    return this.from;
  }

  @Nonnull
  public ItemStack getTo() {
    return this.to;
  }

  /**
   * {@link CurioChangeEvent.Item} is fired when the curio change is due to a difference in items
   * between the previous state and the current state, as returned by
   * {@link ItemStack#isSameItem(ItemStack, ItemStack)}.
   */
  public static class Item extends CurioChangeEvent {

    public Item(LivingEntity living, String type, int index, @Nonnull ItemStack from,
                @Nonnull ItemStack to) {
      super(living, type, index, from, to);
    }
  }

  /**
   * {@link CurioChangeEvent.State} is fired when the curio change is due to a difference in count
   * or components but not items, as returned by {@link ItemStack#matches(ItemStack, ItemStack)}.
   */
  public static class State extends CurioChangeEvent {

    public State(LivingEntity living, String type, int index, @Nonnull ItemStack from,
                 @Nonnull ItemStack to) {
      super(living, type, index, from, to);
    }
  }
}
