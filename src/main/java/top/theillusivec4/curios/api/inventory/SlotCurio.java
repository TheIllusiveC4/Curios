package top.theillusivec4.curios.api.inventory;

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

public final class SlotCurio extends SlotItemHandler {

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
    public void putStack(@Nonnull ItemStack stack) {
        CuriosAPI.getCurio(stack).ifPresent(curio -> curio.onEquipped(stack, curioType.getIdentifier(), player));
        super.putStack(stack);
    }

    @Nonnull
    @Override
    public ItemStack onTake(EntityPlayer thePlayer, @Nonnull ItemStack stack) {
        CuriosAPI.getCurio(stack).ifPresent(curio -> curio.onUnequipped(stack, curioType.getIdentifier(), thePlayer));
        return super.onTake(thePlayer, stack);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return CuriosAPI.getCurioTags(stack.getItem()).contains(curioType.getIdentifier())
                && CuriosAPI.getCurio(stack).map(curio -> curio.canEquip(stack, curioType.getIdentifier(), player)).orElse(true)
                && super.isItemValid(stack);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        ItemStack stack = this.getStack();
        return CuriosAPI.getCurio(stack).map(curio -> ((stack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(stack))
                && curio.canUnequip(stack, curioType.getIdentifier(), playerIn))).orElse(false);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    @Override
    public String getSlotTexture() {
        return slotOverlay;
    }
}
