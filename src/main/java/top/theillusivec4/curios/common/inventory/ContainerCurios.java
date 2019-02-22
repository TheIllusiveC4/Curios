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

package top.theillusivec4.curios.common.inventory;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.CurioType;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.CuriosRegistry;
import top.theillusivec4.curios.api.capability.ICurioItemHandler;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;
import top.theillusivec4.curios.api.inventory.SlotCurio;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketScrollCurios;
import top.theillusivec4.curios.common.network.server.SPacketScrollCurios;

import javax.annotation.Nonnull;
import java.util.SortedMap;

public class ContainerCurios extends Container {

    private static final String[] EMPTY_SLOT_NAMES = new String[]{  "minecraft:item/empty_armor_slot_boots",
                                                                    "minecraft:item/empty_armor_slot_leggings",
                                                                    "minecraft:item/empty_armor_slot_chestplate",
                                                                    "minecraft:item/empty_armor_slot_helmet"};
    private static final EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EntityEquipmentSlot[] {EntityEquipmentSlot.HEAD,
            EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};

    public final LazyOptional<ICurioItemHandler> curios;

    private final EntityPlayer player;
    private final boolean isLocalWorld;

    private InventoryCrafting craftMatrix = new InventoryCrafting(this, 2, 2);
    private InventoryCraftResult craftResult = new InventoryCraftResult();
    private int lastScrollIndex;

    public ContainerCurios(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        this.player = playerIn;
        this.isLocalWorld = playerIn.world.isRemote;
        this.curios = CuriosAPI.getCuriosHandler(playerIn);
        this.addSlot(new SlotCrafting(playerInventory.player, this.craftMatrix, this.craftResult, 0, 154, 28));

        for (int i = 0; i < 2; ++i) {

            for (int j = 0; j < 2; ++j) {
                this.addSlot(new Slot(this.craftMatrix, j + i * 2, 98 + j * 18, 18 + i * 18));
            }
        }

        for (int k = 0; k < 4; ++k) {
            final EntityEquipmentSlot entityequipmentslot = VALID_EQUIPMENT_SLOTS[k];
            this.addSlot(new Slot(playerInventory, 36 + (3 - k), 8, 8 + k * 18) {

                @Override
                public int getSlotStackLimit()
                {
                    return 1;
                }

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack.canEquip(entityequipmentslot, player);
                }

                @Override
                public boolean canTakeStack(EntityPlayer playerIn) {
                    ItemStack itemstack = this.getStack();
                    return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse
                            (itemstack)) && super.canTakeStack(playerIn);
                }

                @OnlyIn(Dist.CLIENT)
                public String getSlotTexture()
                {
                    return EMPTY_SLOT_NAMES[entityequipmentslot.getIndex()];
                }
            });
        }

        for (int l = 0; l < 3; ++l) {

            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
        }
        this.addSlot(new Slot(playerInventory, 40, 77, 62) {
            @OnlyIn(Dist.CLIENT)
            public String getSlotTexture()
            {
                return "minecraft:item/empty_armor_slot_shield";
            }
        });

        this.curios.ifPresent(curios -> {
            SortedMap<String, CurioStackHandler> curioMap = curios.getCurioMap();
            int slots = 0;
            int yOffset = 12;

            for (String identifier : curioMap.keySet()) {
                ItemStackHandler stackHandler = curioMap.get(identifier);
                CurioType type = CuriosRegistry.getType(identifier);

                if (type != null && !type.isHidden()) {

                    for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {
                        this.addSlot(new SlotCurio(player, stackHandler, i, type, -18, yOffset));
                        yOffset += 18;
                        slots++;
                    }
                }
            }
        });
        this.scrollToIndex(0);
    }

    public void scrollToIndex(int indexIn) {
        this.curios.ifPresent(curios -> {
            SortedMap<String, CurioStackHandler> curioMap = curios.getCurioMap();
            int slots = 0;
            int yOffset = 12;
            int index = 0;
            this.inventorySlots.subList(46, this.inventorySlots.size()).clear();
            this.inventoryItemStacks.subList(46, this.inventoryItemStacks.size()).clear();

            for (String identifier : curioMap.keySet()) {
                ItemStackHandler stackHandler = curioMap.get(identifier);

                for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {
                    CurioType type = CuriosRegistry.getType(identifier);

                    if (type != null) {

                        if (index >= indexIn) {
                            this.addSlot(new SlotCurio(player, stackHandler, i, type, -18, yOffset));
                            yOffset += 18;
                            slots++;
                        }
                        index++;
                    }
                }
            }

            if (!this.isLocalWorld) {
                NetworkHandler.INSTANCE.sendTo(new SPacketScrollCurios(this.windowId, indexIn),
                        ((EntityPlayerMP)this.player).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
            }
            lastScrollIndex = indexIn;
        });
    }

    public void scrollTo(float pos) {
        this.curios.ifPresent(curios -> {
            int k = (curios.getSlots() - 8);
            int j = (int)((double)(pos * (float)k) + 0.5D);

            if (j < 0) {
                j = 0;
            }

            if (j == this.lastScrollIndex) {
                return;
            }

            if (this.isLocalWorld) {
                NetworkHandler.INSTANCE.sendToServer(new CPacketScrollCurios(this.windowId, j));
            }
        });
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        this.slotChangedCraftingGrid(this.player.world, this.player, this.craftMatrix, this.craftResult);
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        this.craftResult.clear();

        if (!playerIn.world.isRemote) {
            this.clearContainer(playerIn, playerIn.world, this.craftMatrix);
        }
    }

    public boolean canScroll() {
        return this.curios.map(curios -> {

            if (curios.getSlots() > 8) {
                return 1;
            }
            return 0;
        }).orElse(0) == 1;
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn)
    {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(itemstack);
            if (index == 0) {

                if (!this.mergeItemStack(itemstack1, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(itemstack1, itemstack);
            } else if (index < 5) {

                if (!this.mergeItemStack(itemstack1, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 9) {

                if (!this.mergeItemStack(itemstack1, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (entityequipmentslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR && !(this.inventorySlots.get(8 - entityequipmentslot.getIndex())).getHasStack()) {
                int i = 8 - entityequipmentslot.getIndex();

                if (!this.mergeItemStack(itemstack1, i, i + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 46 && CuriosAPI.getCurio(itemstack).isPresent()) {

                if (this.mergeItemStack(itemstack1, 46, this.inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (entityequipmentslot == EntityEquipmentSlot.OFFHAND && !(this.inventorySlots.get(45)).getHasStack()) {

                if (!this.mergeItemStack(itemstack1, 45, 46, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 9, 45, false)) {
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
            ItemStack itemstack2 = slot.onTake(playerIn, itemstack1);

            if (index == 0) {
                playerIn.dropItem(itemstack2, false);
            }
        }

        return itemstack;
    }
}
