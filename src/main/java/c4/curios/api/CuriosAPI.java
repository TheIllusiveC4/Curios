package c4.curios.api;

import c4.curios.api.capability.CapCurioInventory;
import c4.curios.api.capability.CapCurioItem;
import c4.curios.api.capability.ICurio;
import c4.curios.api.capability.ICurioItemHandler;
import c4.curios.api.inventory.CurioSlotEntry;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CuriosAPI {

    private static Map<String, CurioSlotEntry> idToEntry = new HashMap<>();

    public static CurioSlotEntry createSlot(@Nonnull String identifier) {
        CurioSlotEntry entry = new CurioSlotEntry(identifier);
        idToEntry.put(identifier, entry);
        return entry;
    }

    public static ImmutableMap<String, CurioSlotEntry> getRegistry() {
        return ImmutableMap.copyOf(idToEntry);
    }

    @Nullable
    public static ICurio getCurio(ItemStack stack) {

        if (!stack.isEmpty() && stack.hasCapability(CapCurioItem.CURIO_CAP, CapCurioItem.DEFAULT_FACING)) {
            return stack.getCapability(CapCurioItem.CURIO_CAP, CapCurioItem.DEFAULT_FACING);
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("ConstantConditions")
    public static ICurioItemHandler getCuriosHandler(final EntityLivingBase entityLivingBase) {

        if (entityLivingBase != null && entityLivingBase.hasCapability(CapCurioInventory.CURIO_INV_CAP,
                CapCurioInventory.DEFAULT_FACING)) {
            return entityLivingBase.getCapability(CapCurioInventory.CURIO_INV_CAP, CapCurioInventory.DEFAULT_FACING);
        }
        return null;
    }

    public static void setSlotEnabled(String id, boolean enabled) {
        CurioSlotEntry entry = idToEntry.get(id);

        if (entry != null) {
            entry.setEnabled(enabled);
        }
    }

    public static void addSlotToEntity(String id, final EntityLivingBase entityLivingBase) {
        addSlotsToEntity(id, 1, entityLivingBase);
    }

    public static void addSlotsToEntity(String id, int amount, final EntityLivingBase entityLivingBase) {
        ICurioItemHandler handler = getCuriosHandler(entityLivingBase);

        if (handler != null) {
            ItemStackHandler stackHandler = handler.getCurioMap().get(id);

            if (stackHandler != null) {

                if (amount < 0) {
                    NonNullList<ItemStack> drops = NonNullList.create();

                    for (int i = stackHandler.getSlots() - 1; i >= stackHandler.getSlots() + amount; i--) {
                        ItemStack stack = stackHandler.getStackInSlot(i);
                        drops.add(stackHandler.getStackInSlot(i));
                        ICurio curio = CuriosAPI.getCurio(stack);

                        if (!stack.isEmpty() && curio != null) {
                            curio.onUnequipped(stack, entityLivingBase);
                            entityLivingBase.getAttributeMap().removeAttributeModifiers(curio.getAttributeModifiers(id, stack));
                        }
                        stackHandler.setStackInSlot(i, ItemStack.EMPTY);
                    }

                    if (entityLivingBase instanceof EntityPlayer) {

                        for (ItemStack drop : drops) {
                            ItemHandlerHelper.giveItemToPlayer((EntityPlayer) entityLivingBase, drop);
                        }
                    } else {

                        for (ItemStack drop : drops) {
                            entityLivingBase.entityDropItem(drop, 0.0f);
                        }
                    }
                }
                NonNullList<ItemStack> copy = NonNullList.create();

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    copy.add(stackHandler.getStackInSlot(i).copy());
                }
                stackHandler.setSize(stackHandler.getSlots() + amount);

                for (int i = 0; i < copy.size(); i++) {
                    stackHandler.setStackInSlot(i, copy.get(i));
                }
            }
        }
    }

    public static void enableSlotForEntity(String id, final EntityLivingBase entityLivingBase) {
        ICurioItemHandler handler = getCuriosHandler(entityLivingBase);

        if (handler != null) {
            handler.addCurioSlot(id);
        }
    }

    public static void disableSlotForEntity(String id, final EntityLivingBase entityLivingBase) {
        ICurioItemHandler handler = getCuriosHandler(entityLivingBase);

        if (handler != null) {
            ItemStackHandler stackHandler = handler.getCurioMap().get(id);

            if (stackHandler != null) {

                NonNullList<ItemStack> drops = NonNullList.create();

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);
                    drops.add(stackHandler.getStackInSlot(i));
                    ICurio curio = CuriosAPI.getCurio(stack);

                    if (!stack.isEmpty() && curio != null) {
                        curio.onUnequipped(stack, entityLivingBase);
                        entityLivingBase.getAttributeMap().removeAttributeModifiers(curio.getAttributeModifiers(id, stack));
                    }
                    stackHandler.setStackInSlot(i, ItemStack.EMPTY);
                }

                if (entityLivingBase instanceof EntityPlayer) {

                    for (ItemStack drop : drops) {
                        ItemHandlerHelper.giveItemToPlayer((EntityPlayer) entityLivingBase, drop);
                    }
                } else {

                    for (ItemStack drop : drops) {
                        entityLivingBase.entityDropItem(drop, 0.0f);
                    }
                }
                handler.removeCurioSlot(id);
            }
        }
    }
}
