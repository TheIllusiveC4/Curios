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

import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.SlotItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.extensions.ICurioSlotExtension;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.mixin.core.AccessorEntity;

public class CurioSlot extends SlotItemHandler {

  private final String identifier;
  private final Player player;
  private final SlotContext slotContext;
  private final ICurioSlotExtension extension;

  private final NonNullList<Boolean> renderStatuses;
  private final boolean canToggleRender;
  private List<Boolean> activeStatuses;
  private boolean showCosmeticToggle;
  private boolean isCosmetic;

  public CurioSlot(Player player, IDynamicStackHandler handler, int index, String identifier,
                   int xPosition, int yPosition, NonNullList<Boolean> renders,
                   List<Boolean> actives,
                   boolean canToggleRender, boolean showCosmeticToggle, boolean isCosmetic) {
    this(player, handler, index, identifier, xPosition, yPosition, renders, canToggleRender,
         showCosmeticToggle, isCosmetic);
    this.activeStatuses = actives;
  }

  public CurioSlot(Player player, IDynamicStackHandler handler, int index, String identifier,
                   int xPosition, int yPosition, NonNullList<Boolean> renders,
                   boolean canToggleRender, boolean showCosmeticToggle, boolean isCosmetic) {
    this(player, handler, index, identifier, xPosition, yPosition, renders, canToggleRender);
    this.showCosmeticToggle = showCosmeticToggle;
    this.isCosmetic = isCosmetic;
  }

  public CurioSlot(
      Player player,
      IDynamicStackHandler handler,
      int index,
      String identifier,
      int xPosition,
      int yPosition,
      NonNullList<Boolean> renders,
      boolean canToggleRender) {
    super(handler, index, xPosition, yPosition);
    this.identifier = identifier;
    this.renderStatuses = renders;
    this.player = player;
    this.canToggleRender = canToggleRender;
    this.slotContext =
        new SlotContext(
            identifier,
            player,
            index,
            this instanceof CosmeticCurioSlot,
            this instanceof CosmeticCurioSlot || renders.get(index));
    CuriosApi.getSlot(identifier, player.level())
        .ifPresent(slotType -> this.setBackground(InventoryMenu.BLOCK_ATLAS, slotType.getIcon()));
    this.extension = ICurioSlotExtension.from(identifier);
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

  public boolean showCosmeticToggle() {
    return this.showCosmeticToggle;
  }

  public ICurioSlotExtension getSlotExtension() {
    return this.extension;
  }

  public SlotContext getSlotContext() {
    return this.slotContext;
  }

  public boolean getRenderStatus() {

    if (!this.canToggleRender) {
      return true;
    }
    return this.renderStatuses.size() > this.getSlotIndex()
        && this.renderStatuses.get(this.getSlotIndex());
  }

  @OnlyIn(Dist.CLIENT)
  public String getSlotName() {
    StringBuilder builder = new StringBuilder();

    if (this.isCosmetic) {
      builder.append(I18n.get("curios.cosmetic")).append(" ");
    }
    String key = "curios.identifier." + this.identifier;

    if (I18n.exists(key)) {
      builder.append(I18n.get(key));

      return builder.toString();
    }
    return builder
        .append(Character.toUpperCase(this.identifier.charAt(0)))
        .append(this.identifier.substring(1).toLowerCase())
        .toString();
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
      CuriosApi.getCurio(stack).ifPresent(curio -> curio.onEquipFromUse(this.slotContext));
    }
  }

  @Override
  public boolean allowModification(@Nonnull Player pPlayer) {
    return true;
  }
}
