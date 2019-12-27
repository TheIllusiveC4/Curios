/*
 * Copyright (C) 2018-2019  C4
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

package top.theillusivec4.curios.api.inventory;

import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.SlotItemHandler;
import top.theillusivec4.curios.api.CuriosAPI;

public class SlotCurio extends SlotItemHandler {

  private final String identifier;
  private final PlayerEntity player;

  public SlotCurio(PlayerEntity player, CurioStackHandler handler, int index, String identifier,
      int xPosition, int yPosition) {
    super(handler, index, xPosition, yPosition);
    this.identifier = identifier;
    this.player = player;
    setBackground(PlayerContainer.field_226615_c_, CuriosAPI.getIcon(identifier));
  }

  @OnlyIn(Dist.CLIENT)
  public String getSlotName() {
    return I18n.format("curios.identifier." + identifier);
  }

  @Override
  public boolean isItemValid(@Nonnull ItemStack stack) {
    return hasValidTag(CuriosAPI.getCurioTags(stack.getItem())) && CuriosAPI.getCurio(stack)
        .map(curio -> curio.canEquip(identifier, player)).orElse(true) && super.isItemValid(stack);
  }

  protected boolean hasValidTag(Set<String> tags) {
    return tags.contains(identifier) || tags.contains("curio");
  }

  @Override
  public boolean canTakeStack(PlayerEntity playerIn) {
    ItemStack stack = this.getStack();
    return (stack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(stack))
        && CuriosAPI.getCurio(stack).map(curio -> curio.canUnequip(identifier, playerIn))
        .orElse(true) && super.canTakeStack(playerIn);
  }
}
