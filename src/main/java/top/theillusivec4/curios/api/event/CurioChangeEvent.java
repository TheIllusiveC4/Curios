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
import org.jetbrains.annotations.ApiStatus;
import top.theillusivec4.curios.api.SlotContext;

/**
 * {@link CurioChangeEvent} is fired when the curio item of a LivingEntity changes.
 *
 * <p>This event is fired whenever changes in curios are detected in
 * {@link net.neoforged.neoforge.event.tick.EntityTickEvent}.
 *
 * <p>This also includes entities joining the level, as well as being cloned.
 *
 * <p>This event is fired on server-side only.
 *
 * <br>{@link #slotContext} contains the {@link SlotContext} for the affected slot.
 * <br>{@link #from} contains the {@link ItemStack} that was equipped previously.
 * <br>{@link #to} contains the {@link ItemStack} that is equipped now.
 *
 * <p>This event is fired on the {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS}.
 **/
public abstract class CurioChangeEvent extends LivingEvent {

  private final SlotContext slotContext;
  private final ItemStack from;
  private final ItemStack to;

  /**
   * A constructor that takes a LivingEntity, SlotContext, and the previous/current ItemStacks.
   *
   * @param livingEntity  The {@link LivingEntity} equipping the ItemStacks.
   * @param slotContext   The {@link SlotContext} of the slot that has its contents changed.
   * @param from          The previous {@link ItemStack}.
   * @param to            The current/new {@link ItemStack}.
   */
  @ApiStatus.Internal
  public CurioChangeEvent(LivingEntity livingEntity, @Nonnull SlotContext slotContext,
                          @Nonnull ItemStack from, @Nonnull ItemStack to) {
    super(livingEntity);
    this.slotContext = slotContext;
    this.from = from;
    this.to = to;
  }

  public SlotContext getSlotContext() {
    return this.slotContext;
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

    /**
     * A constructor that takes a LivingEntity, SlotContext, and the previous/current ItemStacks.
     *
     * @param livingEntity  The {@link LivingEntity} equipping the ItemStacks.
     * @param slotContext   The {@link SlotContext} of the slot that has its items changed.
     * @param from          The previous {@link ItemStack}.
     * @param to            The current/new {@link ItemStack}.
     */
    public Item(LivingEntity livingEntity, @Nonnull SlotContext slotContext,
                @Nonnull ItemStack from, @Nonnull ItemStack to) {
      super(livingEntity, slotContext, from, to);
    }
  }

  /**
   * {@link CurioChangeEvent.State} is fired when the curio change is due to a difference in count
   * or components but not items, as returned by {@link ItemStack#matches(ItemStack, ItemStack)}.
   */
  public static class State extends CurioChangeEvent {

    /**
     * A constructor that takes a LivingEntity, SlotContext, and the previous/current ItemStacks.
     *
     * @param livingEntity  The {@link LivingEntity} equipping the ItemStacks.
     * @param slotContext   The {@link SlotContext} of the slot that has its items changed.
     * @param from          The previous {@link ItemStack}.
     * @param to            The current/new {@link ItemStack}.
     *
     */
    public State(LivingEntity livingEntity, @Nonnull SlotContext slotContext,
                 @Nonnull ItemStack from, @Nonnull ItemStack to) {
      super(livingEntity, slotContext, from, to);
    }
  }
}
