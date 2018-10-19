package c4.curios.common.inventory;

import c4.curios.api.event.PlayerCurioChangeEvent;
import c4.curios.api.inventory.CurioSlotInfo;
import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurio;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
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
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        return Math.min(slotInfo.getStackLimit(stack), super.getItemStackLimit(stack));
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        ItemStack stack = this.getStack();
        ICurio curio = CuriosAPI.getCurio(stack);
        return (stack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(stack))
                && (curio == null || curio.canUnequip(stack, playerIn)) && super.canTakeStack(playerIn);
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        ItemStack prevStack = this.getItemHandler().getStackInSlot(index);

        if (!ItemStack.areItemStacksEqual(stack, prevStack)) {
            String identifier = this.slotInfo.getIdentifier();
            MinecraftForge.EVENT_BUS.post(new PlayerCurioChangeEvent(player, identifier, prevStack, stack));
            ICurio prevCurio = CuriosAPI.getCurio(prevStack);
            ICurio curio = CuriosAPI.getCurio(stack);

            if (!prevStack.isEmpty() && prevCurio != null) {
                player.getAttributeMap().removeAttributeModifiers(prevCurio.getAttributeModifiers(identifier, prevStack));
            }

            if (!stack.isEmpty() && curio != null) {
                player.getAttributeMap().applyAttributeModifiers(curio.getAttributeModifiers(identifier, stack));
            }
        }

        super.putStack(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getBackgroundSprite() {
        return slotOverlay;
    }
}
