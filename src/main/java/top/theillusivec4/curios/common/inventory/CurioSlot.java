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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.ClientTooltipFlag;
import net.neoforged.neoforge.items.SlotItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.extensions.ICurioSlotExtension;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.mixin.core.AccessorEntity;

public class CurioSlot extends SlotItemHandler {

  private final String identifier;
  private final Player player;
  private final ICurioSlotExtension extension;

  private final NonNullList<Boolean> renderStatuses;
  private final boolean canToggleRender;
  private List<Boolean> activeStatuses;
  private boolean isCosmetic;

  public CurioSlot(
      Player player,
      IDynamicStackHandler handler,
      int index,
      String identifier,
      int xPosition,
      int yPosition,
      NonNullList<Boolean> renders,
      List<Boolean> actives,
      boolean canToggleRender,
      boolean isCosmetic) {
    super(handler, index, xPosition, yPosition);
    this.identifier = identifier;
    this.renderStatuses = renders;
    this.activeStatuses = actives;
    this.player = player;
    this.canToggleRender = canToggleRender;
    ISlotType slotType = ISlotType.get(identifier);

    if (slotType != null) {
      this.setBackground(slotType.getIcon());
    }
    this.extension = ICurioSlotExtension.from(identifier);
    this.isCosmetic = isCosmetic;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public boolean canToggleRender() {
    return this.canToggleRender;
  }

  public boolean isActiveState() {
    return this.activeStatuses.size() > this.getSlotIndex() && this.activeStatuses.get(
        this.getSlotIndex());
  }

  public boolean isCosmetic() {
    return this.isCosmetic;
  }

  public ICurioSlotExtension getSlotExtension() {
    return this.extension;
  }

  public SlotContext getSlotContext() {
    return new SlotContext(identifier, player, index, isCosmetic,
                           isCosmetic || this.getRenderStatus());
  }

  public boolean getRenderStatus() {

    if (!this.canToggleRender) {
      return true;
    }
    return this.renderStatuses.size() > this.getSlotIndex()
        && this.renderStatuses.get(this.getSlotIndex());
  }

  public List<Component> getSlotTooltip() {
    List<Component> tooltip = new ArrayList<>();
    List<Component> oldTooltipCall = this.extension.getSlotTooltip(
        this.getSlotContext(),
        ClientTooltipFlag.of(Minecraft.getInstance().options.advancedItemTooltips
                             ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL));

    if (!oldTooltipCall.isEmpty()) {
      return oldTooltipCall;
    }
    tooltip.add(
        Component.translatableWithFallback("curios.identifier." + this.identifier,
                                           this.identifier.substring(0, 1).toUpperCase()
                                               + this.identifier.substring(1).toLowerCase()));

    if (this.isCosmetic) {
      tooltip.add(Component.translatable("curios.cosmetic").withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    if (!this.isActiveState()) {
      tooltip.add(Component.translatable("curios.tooltip.inactive").withStyle(ChatFormatting.RED));
    }
    tooltip = this.extension.getSlotTooltip(
        this.getSlotContext(),
        tooltip,
        ClientTooltipFlag.of(Minecraft.getInstance().options.advancedItemTooltips
                             ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL));
    return tooltip;
  }

  @Override
  public void set(@Nonnull ItemStack stack) {
    ItemStack current = this.getItem();
    boolean flag = current.isEmpty() && stack.isEmpty();
    super.set(stack);

    if (!flag
        && !ItemStack.matches(current, stack)
        && !((AccessorEntity) this.player).getFirstTick()
        && this.isActiveState()) {
      CuriosApi.getCurio(stack).ifPresent(curio -> curio.onEquipFromUse(this.getSlotContext()));
    }
  }

  @Override
  public boolean allowModification(@Nonnull Player pPlayer) {
    return true;
  }
}
