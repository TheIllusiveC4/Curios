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

package top.theillusivec4.curios.common.capability;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemizedCurioCapability implements ICurio {
  private final ItemStack stackInstance;
  private final ICurioItem curioItem;

  public ItemizedCurioCapability(ICurioItem curio, ItemStack stack) {
    this.curioItem = curio;
    this.stackInstance = stack;
  }

  @Override
  public boolean canEquip(String identifier, LivingEntity livingEntity) {
    return this.curioItem.canEquip(identifier, livingEntity, this.stackInstance);
  }

  @Override
  public boolean canRender(String identifier, int index, LivingEntity livingEntity) {
    return this.curioItem.canRender(identifier, index, livingEntity, this.stackInstance);
  }

  @Override
  public boolean canSync(String identifier, int index, LivingEntity livingEntity) {
    return this.curioItem.canSync(identifier, index, livingEntity, this.stackInstance);
  }

  @Override
  public boolean canUnequip(String identifier, LivingEntity livingEntity) {
    return this.curioItem.canUnequip(identifier, livingEntity, this.stackInstance);
  }

  @Override
  public void curioAnimate(String identifier, int index, LivingEntity livingEntity) {
    this.curioItem.curioAnimate(identifier, index, livingEntity, this.stackInstance);
  }

  @Override
  public void curioBreak(ItemStack stack, LivingEntity livingEntity) {
    this.curioItem.curioBreak(stack, livingEntity);
  }

  @Override
  public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext,
                                                                      UUID uuid) {
    return this.curioItem.getAttributeModifiers(slotContext, uuid, this.stackInstance);
  }

  @Override
  public Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier) {
    return this.curioItem.getAttributeModifiers(identifier, this.stackInstance);
  }

  @Override
  public void curioTick(String identifier, int index, LivingEntity livingEntity) {
    this.curioItem.curioTick(identifier, index, livingEntity, this.stackInstance);
  }

  @Nonnull
  @Override
  public DropRule getDropRule(LivingEntity livingEntity) {
    return this.curioItem.getDropRule(livingEntity, this.stackInstance);
  }

  @Override
  public int getFortuneBonus(String identifier, LivingEntity livingEntity, ItemStack curioStack,
                             int index) {
    return this.curioItem.getFortuneBonus(identifier, livingEntity, curioStack, index);
  }

  @Override
  public int getLootingBonus(String identifier, LivingEntity livingEntity, ItemStack curioStack,
                             int index) {
    return this.curioItem.getLootingBonus(identifier, livingEntity, curioStack, index);
  }

  @Override
  public List<ITextComponent> getTagsTooltip(List<ITextComponent> tagTooltips) {
    return this.curioItem.getTagsTooltip(tagTooltips, this.stackInstance);
  }

  @Override
  public void onEquip(String identifier, int index, LivingEntity livingEntity) {
    this.curioItem.onEquip(identifier, index, livingEntity, this.stackInstance);
  }

  @Override
  public void onUnequip(String identifier, int index, LivingEntity livingEntity) {
    this.curioItem.onUnequip(identifier, index, livingEntity, this.stackInstance);
  }

  @Override
  public void readSyncData(CompoundNBT compound) {
    this.curioItem.readSyncData(compound, this.stackInstance);
  }

  @Override
  public void render(String identifier, int index, MatrixStack matrixStack,
                     IRenderTypeBuffer renderTypeBuffer, int light, LivingEntity livingEntity,
                     float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                     float netHeadYaw, float headPitch) {
    this.curioItem
        .render(identifier, index, matrixStack, renderTypeBuffer, light, livingEntity, limbSwing,
            limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, this.stackInstance);
  }

  @Override
  public boolean showAttributesTooltip(String identifier) {
    return this.curioItem.showAttributesTooltip(identifier, this.stackInstance);
  }

  @Nonnull
  @Override
  public CompoundNBT writeSyncData() {
    return this.curioItem.writeSyncData(this.stackInstance);
  }

  @Override
  public void onEquipFromUse(SlotContext slotContext) {
    this.curioItem.onEquipFromUse(slotContext, this.stackInstance);
  }

  @Override
  public boolean canEquipFromUse(SlotContext slotContext) {
    return this.curioItem.canEquipFromUse(slotContext, this.stackInstance);
  }

  @Override
  public boolean canRightClickEquip() {
    return this.curioItem.canRightClickEquip(this.stackInstance);
  }

  @Override
  public void playRightClickEquipSound(LivingEntity livingEntity) {
    this.curioItem.playRightClickEquipSound(livingEntity, this.stackInstance);
  }
}
