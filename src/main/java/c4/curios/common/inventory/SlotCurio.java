package c4.curios.common.inventory;

import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurio;
import c4.curios.api.inventory.CurioSlotEntry;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SlotCurio extends SlotItemHandler {

    private final CurioSlotEntry entry;
    private final EntityPlayer player;
    private final String slotOverlay;

    public SlotCurio(EntityPlayer player, IItemHandler handler, int index, CurioSlotEntry info, int xPosition,
                     int yPosition) {
        super(handler, index, xPosition, yPosition);
        this.entry = info;
        this.player = player;
        this.slotOverlay = entry.getIcon() == null ? null : entry.getIcon().toString();
    }

    public String getSlotName() {
        return entry.getFormattedName();
    }

    public CurioSlotEntry getCurioSlotEntry() { return entry; }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        ICurio curio = CuriosAPI.getCurio(stack);
        if (curio != null) {
            return curio.getCurioSlots(stack).contains(entry.getIdentifier()) && curio.canEquip(stack, player)
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

    @Nullable
    @SideOnly(Side.CLIENT)
    @Override
    public String getSlotTexture() {
        return slotOverlay;
    }
}
