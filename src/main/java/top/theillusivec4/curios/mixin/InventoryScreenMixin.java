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

package top.theillusivec4.curios.mixin;

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.client.screen.CuriosButton;
import top.theillusivec4.curios.client.screen.CuriosScreen;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {

  public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory,
      Text text) {
    super(screenHandler, playerInventory, text);
  }

  @Inject(method = "init", at = @At(value = "INVOKE_ASSIGN", target = "net/minecraft/client/gui/screen/ingame/InventoryScreen.addButton (Lnet/minecraft/client/gui/widget/AbstractButtonWidget;)Lnet/minecraft/client/gui/widget/AbstractButtonWidget;"))
  public void init(CallbackInfo cb) {
    Pair<Integer, Integer> offsets = CuriosScreen.getButtonOffset(false);
    int x = offsets.getLeft();
    int y = offsets.getRight();
    int size = 14;
    int textureOffsetX = 50;
    this.addButton(
        new CuriosButton(this, this.x + x, this.height / 2 + y, size, size, textureOffsetX, 0, size,
            CuriosScreen.CURIO_INVENTORY));
  }
}
