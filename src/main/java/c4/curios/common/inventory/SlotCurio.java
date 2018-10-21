package c4.curios.common.inventory;

import c4.curios.api.event.LivingChangeCurioEvent;
import c4.curios.api.inventory.CurioSlotInfo;
import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurio;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotCurio extends SlotItemHandler {

    private final CurioSlotInfo slotInfo;
    private final EntityPlayer player;
    private final TextureAtlasSprite slotOverlay;
    private final int index;

    public SlotCurio(EntityPlayer player, IItemHandler handler, int index, CurioSlotInfo info, int xPosition,
                     int yPosition) {
        super(handler, index, xPosition, yPosition);
        this.slotInfo = info;
        this.player = player;
        this.slotOverlay = CuriosAPI.getSpriteFromID(slotInfo.getIdentifier());
        this.index = index;
    }

    public String getSlotName() {
        return slotInfo.getFormattedName();
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        ICurio curio = CuriosAPI.getCurio(stack);
        if (curio != null) {
            return curio.getCurioSlots(stack).contains(slotInfo.getIdentifier()) && curio.canEquip(stack, player)
                    && super.isItemValid(stack);
        }
        return false;
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        ItemStack stack = this.getStack();
        ICurio curio = CuriosAPI.getCurio(stack);
        return (stack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(stack))
                && (curio == null || curio.canUnequip(stack, playerIn)) && super.canTakeStack(playerIn);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getBackgroundSprite() {
        return slotOverlay;
    }
}
