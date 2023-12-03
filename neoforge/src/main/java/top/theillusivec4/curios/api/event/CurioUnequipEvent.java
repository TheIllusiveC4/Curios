/*
 * Copyright (c) 2018-2023 C4
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

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import top.theillusivec4.curios.api.SlotContext;

/**
 * CurioUnequipEvent is fired when a curio item is about to be unequipped and allows an event
 * listener to specify whether it should or not. <br>
 * This event is fired when ever the {@link top.theillusivec4.curios.api.type.capability.ICurio#canUnequip(SlotContext)}
 * is checked. <br>
 * <br>
 * This event has a {@link Event.Result result}:
 * <ul><li>{@link Result#ALLOW} means the curio item can be unequipped.</li>
 * <li>{@link Result#DEFAULT} means {@link top.theillusivec4.curios.api.type.capability.ICurio#canUnequip(SlotContext)}
 * determines the result.</li>
 * <li>{@link Result#DENY} means the curio item cannot be unequipped.</li></ul><br>
 * This event is fired on the {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS}.
 */
public class CurioUnequipEvent extends LivingEvent {

  private final SlotContext slotContext;
  private final ItemStack stack;
  private Event.Result result;

  public CurioUnequipEvent(ItemStack stack, SlotContext slotContext) {
    super(slotContext.entity());
    this.slotContext = slotContext;
    this.stack = stack;
  }

  public Result getUnequipResult() {
    return this.result;
  }

  public void setUnequipResult(Result result) {
    this.result = result;
  }

  @Override
  public void setResult(Result result) {
    this.setUnequipResult(result);
  }

  public SlotContext getSlotContext() {
    return slotContext;
  }

  public ItemStack getStack() {
    return stack;
  }
}
