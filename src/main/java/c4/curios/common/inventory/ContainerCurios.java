package c4.curios.common.inventory;

import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurio;
import c4.curios.api.capability.ICurioItemHandler;
import c4.curios.api.inventory.CurioStackHandler;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class ContainerCurios extends Container {

    private static final EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EntityEquipmentSlot[] {EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};
    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 2, 2);
    public InventoryCraftResult craftResult = new InventoryCraftResult();
    private final EntityPlayer player;
    public final ICurioItemHandler curios;

    public ContainerCurios(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        this.player = playerIn;
        this.curios = CuriosAPI.getCuriosHandler(playerIn);
        this.addSlotToContainer(new SlotCrafting(playerInventory.player, this.craftMatrix, this.craftResult, 0, 154, 28));

        for (int i = 0; i < 2; ++i) {

            for (int j = 0; j < 2; ++j) {
                this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 2, 98 + j * 18, 18 + i * 18));
            }
        }

        for (int k = 0; k < 4; ++k) {
            final EntityEquipmentSlot entityequipmentslot = VALID_EQUIPMENT_SLOTS[k];
            this.addSlotToContainer(new Slot(playerInventory, 36 + (3 - k), 8, 8 + k * 18) {
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
                public boolean isItemValid(ItemStack stack) {
                    return stack.getItem().isValidArmor(stack, entityequipmentslot, player);
                }
                /**
                 * Return whether this slot's stack can be taken from this slot.
                 */
                public boolean canTakeStack(EntityPlayer playerIn) {
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

        for (int l = 0; l < 3; ++l) {

            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlotToContainer(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
        }
        this.addSlotToContainer(new Slot(playerInventory, 40, 77, 62) {
            @SideOnly(Side.CLIENT)
            public String getSlotTexture()
            {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });

        if (curios != null) {
            Map<String, CurioStackHandler> curioMap = this.curios.getCurioMap();
            int slots = 0;
            int yOffset = 12;

            for (String identifier : curioMap.keySet()) {
                CurioStackHandler stackHandler = curioMap.get(identifier);

                for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {
                    this.addSlotToContainer(new SlotCurio(player, stackHandler, i, stackHandler.getEntry(), -18, yOffset));
                    yOffset += 18;
                    slots++;
                }
            }
        }
        this.scrollTo(0.0F);
    }

    public void scrollTo(float pos) {
        int j = (int)((double)(pos * (float)this.curios.getSlots()) + 0.5D);

        if (j < 0) {
            j = 0;
        }
        Map<String, CurioStackHandler> curioMap = this.curios.getCurioMap();
        int slots = 0;
        int yOffset = 12;

        for (String identifier : curioMap.keySet()) {
            CurioStackHandler stackHandler = curioMap.get(identifier);

            for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {
                this.inventorySlots.set(46 + slots, new SlotCurio(player, stackHandler, i, stackHandler.getEntry(), -18, yOffset));
                yOffset += 18;
                slots++;
            }
        }

        for (int k = 0; k < 5; ++k) {

            for (int l = 0; l < 9; ++l) {
                int i1 = l + (k + j) * 9;

                if (i1 >= 0 && i1 < this.curios.getSlots()) {
//                    this.inventorySlots.set(46, new SlotCurio())
                } else {
//                    GuiContainerCreative.basicInventory.setInventorySlotContents(l + k * 9, ItemStack.EMPTY);
                }
            }
        }
    }

    public boolean canScroll() {
        return this.curios.getSlots() > 45;
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
                ICurioItemHandler curioHandler = CuriosAPI.getCuriosHandler(playerIn);

                if (curio != null && curioHandler != null) {
                    Map<String, CurioStackHandler> curioSlots = curioHandler.getCurioMap();
                    int curioIndex = 0;

                    for (String identifier : curioSlots.keySet()) {
                        CurioStackHandler stackHandler = curioSlots.get(identifier);

                        for (int i = 0; i < stackHandler.getSlots(); i++) {
                            ItemStack stack = stackHandler.getStackInSlot(i);

                            if (curio.getCurioSlots(itemstack1).contains(identifier) && curio.canEquip(itemstack1, playerIn)
                                    && stack.isEmpty() && !this.mergeItemStack(itemstack1, 41 + curioIndex,
                                    41 + curioIndex + 1, false)) {
                                return ItemStack.EMPTY;
                            }
                            curioIndex++;
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
