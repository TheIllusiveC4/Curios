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

import java.util.function.Function;
import javax.annotation.Nonnull;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;
import top.theillusivec4.curios.api.event.CurioCanUnequipEvent;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

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
    SlotContext ctx = this.ctxBuilder.apply(slot);
    boolean originalResult = (CuriosApi.isStackValid(ctx, stack)
        && CuriosApi.getCurio(stack).map(curio -> curio.canEquip(ctx)).orElse(true)
        && super.isItemValid(slot, stack));
    CurioCanEquipEvent equipEvent = new CurioCanEquipEvent(stack, ctx, originalResult);
    NeoForge.EVENT_BUS.post(equipEvent);
    TriState result = equipEvent.getEquipResult();
    return (result == TriState.DEFAULT && originalResult) || result == TriState.TRUE;
  }

  @Override
  public @Nonnull ItemStack extractItem(int slot, int amount, boolean simulate) {
    ItemStack existing = this.stacks.get(slot);
    SlotContext ctx = this.ctxBuilder.apply(slot);
    boolean isCreative = ctx.entity() instanceof Player player && player.isCreative();
    boolean originalResult = (existing.isEmpty()
        || isCreative
        || !EnchantmentHelper.has(existing, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE))
        && CuriosApi.getCurio(existing).map(curio -> curio.canUnequip(ctx)).orElse(true);
    CurioCanUnequipEvent unequipEvent = new CurioCanUnequipEvent(existing, ctx, originalResult);
    NeoForge.EVENT_BUS.post(unequipEvent);
    TriState result = unequipEvent.getUnequipResult();

    if ((result == TriState.DEFAULT && originalResult) || result == TriState.TRUE) {
      return super.extractItem(slot, amount, simulate);
    }
    return ItemStack.EMPTY;
  }

  @Override
  public void grow(int amount) {
    this.stacks = getResizedList(this.stacks.size() + amount, this.stacks);
    this.previousStacks = getResizedList(this.previousStacks.size() + amount, this.previousStacks);
  }

  @Override
  public void shrink(int amount) {
    this.stacks = getResizedList(this.stacks.size() - amount, this.stacks);
    this.previousStacks = getResizedList(this.previousStacks.size() - amount, this.previousStacks);
  }

  @Override
  public CompoundTag serializeNBT(HolderLookup.Provider provider) {
    ListTag nbtTagList = new ListTag();

    for (int i = 0; i < this.stacks.size(); i++) {

      if (!this.stacks.get(i).isEmpty()) {
        CompoundTag itemTag = new CompoundTag();
        itemTag.putInt("Slot", i);
        ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, this.stacks.get(i))
            .ifSuccess(tag -> itemTag.put("Stack", tag));
        nbtTagList.add(itemTag);
      }
    }
    CompoundTag nbt = new CompoundTag();
    nbt.put("Items", nbtTagList);
    nbt.putInt("Size", this.stacks.size());
    return nbt;
  }

  @Override
  public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
    this.setSize(nbt.getIntOr("Size", this.stacks.size()));
    nbt.getListOrEmpty("Items").compoundStream().forEach(itemTags -> {
      int slot = itemTags.getIntOr("Slot", -1);

      if (slot >= 0 && slot < this.stacks.size()) {
        ItemStack[] stack = {ItemStack.EMPTY};
        Tag tag = itemTags.get("Stack");

        if (tag != null) {
          ItemStack.CODEC.decode(NbtOps.INSTANCE, tag).ifSuccess(s -> stack[0] = s.getFirst());
        }
        this.stacks.set(slot, stack[0]);
      }
    });
    this.onLoad();
  }

  private static NonNullList<ItemStack> getResizedList(int size, NonNullList<ItemStack> stacks) {
    NonNullList<ItemStack> newList = NonNullList.withSize(Math.max(0, size), ItemStack.EMPTY);

    for (int i = 0; i < newList.size() && i < stacks.size(); i++) {
      newList.set(i, stacks.get(i));
    }
    return newList;
  }
}
