package c4.curios.common.inventory;

import c4.curios.api.capability.ICurio;
import c4.curios.api.inventory.CurioSlot;
import c4.curios.api.inventory.CurioSlotInfo;
import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurioItemHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContainerCurios extends Container {

    private static final EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EntityEquipmentSlot[] {EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};
    /** Determines if inventory manipulation should be handled. */
    private final EntityPlayer player;
    private final ICurioItemHandler curios;

    public ContainerCurios(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        this.player = playerIn;
        this.curios = CuriosAPI.getCuriosHandler(playerIn);

        for (int k = 0; k < 4; ++k)
        {
            final EntityEquipmentSlot entityequipmentslot = VALID_EQUIPMENT_SLOTS[k];
            this.addSlotToContainer(new Slot(playerInventory, 36 + (3 - k), 8, 8 + k * 18)
            {
                /**
                 * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1
                 * in the case of armor slots)
                 */
                public int getSlotStackLimit()
                {
                    return 1;
                }
                /**
                 * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace
                 * fuel.
                 */
                public boolean isItemValid(ItemStack stack)
                {
                    return stack.getItem().isValidArmor(stack, entityequipmentslot, player);
                }
                /**
                 * Return whether this slot's stack can be taken from this slot.
                 */
                public boolean canTakeStack(EntityPlayer playerIn)
                {
                    ItemStack itemstack = this.getStack();
                    return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse
                            (itemstack)) && super.canTakeStack(playerIn);
                }
                @Nullable
                @SideOnly(Side.CLIENT)
                public String getSlotTexture()
                {
                    return ItemArmor.EMPTY_SLOT_NAMES[entityequipmentslot.getIndex()];
                }
            });
        }

        for (int l = 0; l < 3; ++l)
        {
            for (int j1 = 0; j1 < 9; ++j1)
            {
                this.addSlotToContainer(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1)
        {
            this.addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
        }

        this.addSlotToContainer(new Slot(playerInventory, 40, 77, 62)
        {
            @SideOnly(Side.CLIENT)
            public String getSlotTexture()
            {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });

        if (curios != null) {
            NonNullList<CurioSlot> curioStacks = curios.getCurioStacks();
            int slotCount = curioStacks.size();
            int yOffset = 18;
            for (int k = 0; k < Math.min(3, slotCount / 3 + 1); k++) {
                int xOffset = 98;
                for (int l = 0; l < Math.min(3, slotCount - (k * 3)); l++) {
                    int index = k * 3 + l;
                    this.addSlotToContainer(new SlotCurio(player, curios, index, curioStacks.get(index).getInfo(),
                            xOffset, yOffset));
                    xOffset += 18;
                }
                yOffset += 18;
            }
        }
    }
    /**
     * Determines whether supplied player can use this container
     */
    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn)
    {
        return true;
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(itemstack);

            if (entityequipmentslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR && !(this.inventorySlots.get(3 -
                    entityequipmentslot.getIndex())).getHasStack()) {
                int i = 3 - entityequipmentslot.getIndex();

                if (!this.mergeItemStack(itemstack1, i, i + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (entityequipmentslot == EntityEquipmentSlot.OFFHAND && !(this.inventorySlots.get(40)).getHasStack()) {

                if (!this.mergeItemStack(itemstack1, 40, 41, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 41 && CuriosAPI.getCurio(itemstack1) != null) {
                ICurio curio = CuriosAPI.getCurio(itemstack1);
                assert curio != null;
                ICurioItemHandler curioHandler = CuriosAPI.getCuriosHandler(playerIn);
                if (curioHandler != null) {
                    NonNullList<CurioSlot> curioSlots = curioHandler.getCurioStacks();
                    for (int i = 0; i < curioSlots.size(); i++) {
                        CurioSlot curioSlot = curioSlots.get(i);
                        if (curio.getCurioSlots(itemstack1).contains(curioSlot.getInfo().getIdentifier())) {
                            ItemStack currentCurio = curioSlot.getStack();
                            if (curio.canEquip(itemstack1, playerIn) && currentCurio.isEmpty()
                                    && !this.mergeItemStack(itemstack1, 41 + i, 41 + i + 1, false)) {
                                return ItemStack.EMPTY;
                            }
                        }
                    }
                }
            } else if (index >= 4 && index < 31) {

                if (!this.mergeItemStack(itemstack1, 31, 40, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 31 && index < 40) {

                if (!this.mergeItemStack(itemstack1, 4, 31, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 4, 40, false)) {
                return ItemStack.EMPTY;
            }


            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
        }

        return itemstack;
    }
}
