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

package top.theillusivec4.curios.common.capability;

import com.google.common.collect.Multimap;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemizedCurioCapability implements ICurio {

  private final ItemStack stack;
  private final ICurioItem curioItem;

  public ItemizedCurioCapability(ICurioItem curio, ItemStack stack) {
    this.curioItem = curio;
    this.stack = stack;
  }

  @Override
  public ItemStack getStack() {
    return this.stack;
  }

  @Override
  public void curioTick(SlotContext slotContext) {
    this.curioItem.curioTick(slotContext, this.getStack());
  }

  @Override
  public boolean canEquip(SlotContext slotContext) {
    return this.curioItem.canEquip(slotContext, this.getStack());
  }

  @Override
  public boolean canUnequip(SlotContext slotContext) {
    return this.curioItem.canUnequip(slotContext, this.getStack());
  }

  @Override
  public List<Component> getSlotsTooltip(List<Component> tooltips, Item.TooltipContext context) {
    return this.curioItem.getSlotsTooltip(tooltips, context, this.getStack());
  }

  @Override
  public void curioBreak(SlotContext slotContext) {
    this.curioItem.curioBreak(slotContext, this.getStack());
  }

  @Override
  public boolean canSync(SlotContext slotContext) {
    return this.curioItem.canSync(slotContext, this.getStack());
  }

  @Nonnull
  @Override
  public CompoundTag writeSyncData(SlotContext slotContext) {
    return this.curioItem.writeSyncData(slotContext, this.getStack());
  }

  @Override
  public void readSyncData(SlotContext slotContext, CompoundTag compound) {
    this.curioItem.readSyncData(slotContext, compound, this.getStack());
  }

  @Nonnull
  @Override
  public DropRule getDropRule(SlotContext slotContext, DamageSource source, boolean recentlyHit) {
    return this.curioItem.getDropRule(slotContext, source, recentlyHit, this.getStack());
  }

  @Override
  public List<Component> getAttributesTooltip(List<Component> tooltips,
                                              Item.TooltipContext context) {
    return this.curioItem.getAttributesTooltip(tooltips, context, this.getStack());
  }

  @Override
  public int getFortuneLevel(SlotContext slotContext, LootContext lootContext) {
    return this.curioItem.getFortuneLevel(slotContext, lootContext, this.getStack());
  }

  @Override
  public int getLootingLevel(SlotContext slotContext, @Nullable LootContext lootContext) {
    return this.curioItem.getLootingLevel(slotContext, lootContext, this.getStack());
  }

  @Override
  public CurioAttributeModifiers getDefaultCurioAttributeModifiers() {
    return this.curioItem.getDefaultCurioAttributeModifiers(this.getStack());
  }

  @Override
  public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
      SlotContext slotContext, ResourceLocation id) {
    return this.curioItem.getAttributeModifiers(slotContext, id, this.getStack());
  }

  @Override
  public void onEquipFromUse(SlotContext slotContext) {
    this.curioItem.onEquipFromUse(slotContext, this.getStack());
  }

  @Override
  public boolean canEquipFromUse(SlotContext slotContext) {
    return this.curioItem.canEquipFromUse(slotContext, this.getStack());
  }

  @Override
  public void onEquip(SlotContext slotContext, ItemStack prevStack) {
    this.curioItem.onEquip(slotContext, prevStack, this.getStack());
  }

  @Override
  public void onStateChange(SlotContext slotContent, ItemStack prevStack) {
    this.curioItem.onStateChange(slotContent, prevStack, this.getStack());
  }

  @Override
  public void onUnequip(SlotContext slotContext, ItemStack newStack) {
    this.curioItem.onUnequip(slotContext, newStack, this.getStack());
  }

  @Nonnull
  @Override
  public SoundInfo getEquipSound(SlotContext slotContext) {
    return this.curioItem.getEquipSound(slotContext, this.getStack());
  }

  @Override
  public boolean makesPiglinsNeutral(SlotContext slotContext) {
    return this.curioItem.makesPiglinsNeutral(slotContext, this.getStack());
  }

  @Override
  public boolean isEnderMask(SlotContext slotContext, EnderMan enderMan) {
    return this.curioItem.isEnderMask(slotContext, enderMan, this.getStack());
  }

  @Override
  public boolean canWalkOnPowderedSnow(SlotContext slotContext) {
    return this.curioItem.canWalkOnPowderedSnow(slotContext, this.getStack());
  }
}
