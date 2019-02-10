package c4.curios.api;

import c4.curios.api.capability.CapCurioInventory;
import c4.curios.api.capability.CapCurioItem;
import c4.curios.api.capability.ICurio;
import c4.curios.api.capability.ICurioItemHandler;
import c4.curios.api.inventory.CurioSlotEntry;
import c4.curios.api.inventory.CurioStackHandler;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public static void disableSlot(String id) {
        CurioSlotEntry entry = idToEntry.get(id);

        if (entry != null) {
            entry.disable();
        }
    }

    public static void addSlotToEntity(String id, final EntityLivingBase entityLivingBase) {
        addSlotsToEntity(id, 1, entityLivingBase);
    }

    public static void addSlotsToEntity(String id, int amount, final EntityLivingBase entityLivingBase) {
        ICurioItemHandler handler = getCuriosHandler(entityLivingBase);

        if (handler != null) {
            CurioStackHandler stackHandler = handler.getCurioMap().get(id);

            if (stackHandler != null) {
                stackHandler.setSize(stackHandler.getSlots() + amount, entityLivingBase);
            }
        }
    }

    public static void enableSlotForEntity(String id, final EntityLivingBase entityLivingBase) {
        ICurioItemHandler handler = getCuriosHandler(entityLivingBase);

        if (handler != null) {
            CurioStackHandler stackHandler = handler.getCurioMap().get(id);

            if (stackHandler != null) {
                stackHandler.setEnabled(true);
            }
        }
    }

    public static void disableSlotForEntity(String id, final EntityLivingBase entityLivingBase) {
        ICurioItemHandler handler = getCuriosHandler(entityLivingBase);

        if (handler != null) {
            CurioStackHandler stackHandler = handler.getCurioMap().get(id);

            if (stackHandler != null) {
                stackHandler.setEnabled(false);

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);

                    if (!stack.isEmpty()) {
                        ItemStack copy = stack.copy();

                        if (entityLivingBase instanceof EntityPlayer) {
                            ItemHandlerHelper.giveItemToPlayer((EntityPlayer)entityLivingBase, copy);
                        } else {
                            entityLivingBase.entityDropItem(copy, 0.0f);
                        }
                        stackHandler.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            }
        }
    }
}
