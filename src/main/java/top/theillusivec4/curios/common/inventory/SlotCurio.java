package top.theillusivec4.curios.common.inventory;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import top.theillusivec4.curios.api.CurioType;
import top.theillusivec4.curios.api.CuriosAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SlotCurio extends SlotItemHandler {

    private final CurioType curioType;
    private final EntityPlayer player;
    private final String slotOverlay;

    public SlotCurio(EntityPlayer player, IItemHandler handler, int index, CurioType type, int xPosition, int yPosition) {
        super(handler, index, xPosition, yPosition);
        this.curioType = type;
        this.player = player;
        this.slotOverlay = curioType.getIcon() == null ? "curios:item/empty_generic_slot" : curioType.getIcon().toString();
    }

    public String getSlotName() {
        return curioType.getFormattedName();
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return CuriosAPI.getCurio(stack).map(curio -> {
            if (curio.getCurioTypes(stack).contains(curioType.getIdentifier()) && curio.canEquip(stack, curioType.getIdentifier(), player)
                    && super.isItemValid(stack)) {
                return 1;
            }
            return 0;
        }).orElse(0) == 1;
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        ItemStack stack = this.getStack();
        return CuriosAPI.getCurio(stack).map(curio -> {
            if ((stack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(stack)) && curio.canUnequip(stack, curioType.getIdentifier(), playerIn)
                    && super.canTakeStack(playerIn)) {
                return 1;
            }
            return 0;
        }).orElse(0) == 1;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    @Override
    public String getSlotTexture() {
        return slotOverlay;
    }
}
