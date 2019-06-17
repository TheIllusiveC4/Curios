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

import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.SlotItemHandler;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class SlotCurio extends SlotItemHandler {

    private final String identifier;
    private final PlayerEntity player;

    public SlotCurio(PlayerEntity player, CurioStackHandler handler, int index, String identifier, int xPosition, int yPosition) {
        super(handler, index, xPosition, yPosition);
        this.identifier = identifier;
        this.player = player;
    }

    @OnlyIn(Dist.CLIENT)
    public String getSlotName() {
        String key = "curios.identifier." + identifier;
        if (!I18n.hasKey(key)) {
            return identifier.substring(0, 1).toUpperCase() + identifier.substring(1);
        }
        return I18n.format(key);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return hasValidTag(CuriosAPI.getCurioTags(stack.getItem())) && CuriosAPI.getCurio(stack)
                .map(curio -> curio.canEquip(identifier, player)).orElse(true)
                && super.isItemValid(stack);
    }

    protected boolean hasValidTag(Set<String> tags) {
        return tags.contains(identifier);
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) {
        ItemStack stack = this.getStack();
        return (stack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(stack))
                && CuriosAPI.getCurio(stack).map(curio -> curio.canUnequip(identifier, playerIn)).orElse(true)
                && super.canTakeStack(playerIn);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    @Override
    public String getSlotTexture() {
        return CuriosAPI.getIcons().getOrDefault(identifier, new ResourceLocation(Curios.MODID, "item/empty_generic_slot")).toString();
    }
}
