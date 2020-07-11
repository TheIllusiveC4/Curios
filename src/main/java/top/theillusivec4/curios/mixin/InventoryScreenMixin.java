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
