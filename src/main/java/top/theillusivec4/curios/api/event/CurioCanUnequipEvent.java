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

import net.minecraft.util.TriState;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import top.theillusivec4.curios.api.SlotContext;

/**
 * CurioCanUnequipEvent is fired when a curio item is about to be unequipped and allows an event
 * listener to specify whether it should or not.
 *
 * <p>This event is fired when
 * {@link top.theillusivec4.curios.api.type.capability.ICurio#canUnequip(SlotContext)} is checked.
 *
 * <p>This event has a {@link TriState result}:
 * <ul><li>{@link TriState#TRUE} means the curio item can be unequipped.</li>
 * <li>{@link TriState#DEFAULT} means
 * {@link top.theillusivec4.curios.api.type.capability.ICurio#canUnequip(SlotContext)}
 * determines the result.</li>
 * <li>{@link TriState#FALSE} means the curio item cannot be unequipped.</li></ul><br>
 *
 * <p>This event is fired on the {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS}.
 */
public class CurioCanUnequipEvent extends LivingEvent {

  private final SlotContext slotContext;
  private final ItemStack stack;
  private TriState result;

  public CurioCanUnequipEvent(ItemStack stack, SlotContext slotContext) {
    super(slotContext.entity());
    this.slotContext = slotContext;
    this.stack = stack;
  }

  public TriState getUnequipResult() {
    return this.result;
  }

  public void setUnequipResult(TriState result) {
    this.result = result;
  }

  public SlotContext getSlotContext() {
    return slotContext;
  }

  public ItemStack getStack() {
    return stack;
  }
}
