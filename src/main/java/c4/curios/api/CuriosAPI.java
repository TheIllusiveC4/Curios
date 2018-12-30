package c4.curios.api;

import c4.curios.api.capability.CapCurioInventory;
import c4.curios.api.capability.CapCurioItem;
import c4.curios.api.capability.ICurio;
import c4.curios.api.capability.ICurioItemHandler;
import c4.curios.api.event.LivingGetCuriosEvent;
import c4.curios.api.inventory.CurioSlotEntry;
import c4.curios.api.inventory.CurioStackHandler;
import com.google.common.collect.Maps;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

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

    public static Set<String> getRegisteredIds() {
        return idToEntry.keySet();
    }

    public static CurioSlotEntry getSlotEntryForID(String identifier) {
        return idToEntry.get(identifier);
    }

    public static Map<String, CurioStackHandler> getSlotsMap(EntityLivingBase livingBase) {
        Map<String, CurioStackHandler> map = Maps.newLinkedHashMap();

        for (String id : idToEntry.keySet()) {
            CurioSlotEntry entry = idToEntry.get(id);
            CurioStackHandler stackHandler = new CurioStackHandler(entry);
            LivingGetCuriosEvent evt = new LivingGetCuriosEvent(livingBase, id, stackHandler.getSlots());
            if (!MinecraftForge.EVENT_BUS.post(evt)) {
                stackHandler.setSize(evt.getSize(), livingBase);
                map.put(id, stackHandler);
            }
        }
        return map;
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
}
