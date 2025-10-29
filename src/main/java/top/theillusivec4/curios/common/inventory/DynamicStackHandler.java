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

package top.theillusivec4.curios.common.inventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;
import top.theillusivec4.curios.api.event.CurioCanUnequipEvent;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class DynamicStackHandler extends ItemStackHandler implements IDynamicStackHandler {

  protected NonNullList<ItemStack> previousStacks;
  protected Function<Integer, SlotContext> ctxBuilder;

  public DynamicStackHandler(int size, Function<Integer, SlotContext> ctxBuilder) {
    super(size);
    this.previousStacks = NonNullList.withSize(size, ItemStack.EMPTY);
    this.ctxBuilder = ctxBuilder;
  }

  @Override
  public void setPreviousStackInSlot(int slot, @Nonnull ItemStack stack) {
    this.validateSlotIndex(slot);
    this.previousStacks.set(slot, stack);
    this.onContentsChanged(slot);
  }

  @Nonnull
  @Override
  public ItemStack getPreviousStackInSlot(int slot) {
    this.validateSlotIndex(slot);
    return this.previousStacks.get(slot);
  }

  @Override
  public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
    SlotContext ctx = ctxBuilder.apply(slot);
    boolean canEquip = (CuriosApi.isStackValid(ctx, stack) &&
        CuriosApi.getCurio(stack).map(curio -> curio.canEquip(ctx)).orElse(true) &&
        super.isItemValid(slot, stack));
    CurioCanEquipEvent event =
        new CurioCanEquipEvent(stack, ctx, canEquip ? TriState.TRUE : TriState.FALSE);
    NeoForge.EVENT_BUS.post(event);
    return event.getEquipResult() != TriState.FALSE;
  }

  @Override
  public @Nonnull ItemStack extractItem(int slot, int amount, boolean simulate) {
    ItemStack existing = this.stacks.get(slot);
    SlotContext ctx = ctxBuilder.apply(slot);
    CurioCanUnequipEvent unequipEvent = new CurioCanUnequipEvent(existing, ctx);
    NeoForge.EVENT_BUS.post(unequipEvent);
    TriState result = unequipEvent.getUnequipResult();

    if (result == TriState.FALSE) {
      return ItemStack.EMPTY;
    }
    boolean isCreative = ctx.entity() instanceof Player player && player.isCreative();

    if (result == TriState.TRUE ||
        ((existing.isEmpty() || isCreative ||
            !EnchantmentHelper.has(existing, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) &&
            CuriosApi.getCurio(existing).map(curio -> curio.canUnequip(ctx)).orElse(true))) {
      return super.extractItem(slot, amount, simulate);
    }
    return ItemStack.EMPTY;
  }

	/* ---------- Resizing ---------- */

	// Keep previousStacks in sync whenever the base list size changes
	@Override
	public void setSize(int size) {
		super.setSize(size);
		this.previousStacks = resizeList(size, this.previousStacks);
	}

	@Override
	public void grow(int amount) {
		setSize(this.getSlots() + amount);
	}

	@Override
	public void shrink(int amount) {
		setSize(Math.max(0, this.getSlots() - amount));
	}

	private static NonNullList<ItemStack> resizeList(int size, NonNullList<ItemStack> source) {
		NonNullList<ItemStack> dst = NonNullList.withSize(Math.max(0, size), ItemStack.EMPTY);
		for (int i = 0; i < dst.size() && i < source.size(); i++) {
			dst.set(i, source.get(i));
		}
		return dst;
	}

	/* ---------- Legacy NBT bridge (kept for API compatibility) ---------- */

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
		this.serialize(out);                 // ValueIOSerializable from ItemStackHandler
		return out.buildResult();
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		ValueInput in = TagValueInput.create(
				ProblemReporter.DISCARDING,
				provider,
				nbt == null ? new CompoundTag() : nbt
		);
		this.deserialize(in);                // ValueIOSerializable from ItemStackHandler
	}
}
