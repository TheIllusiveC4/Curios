package c4.curios.api;

import c4.curios.api.capability.CapCurioInventory;
import c4.curios.api.capability.CapCurioItem;
import c4.curios.api.capability.ICurio;
import c4.curios.api.capability.ICurioItemHandler;
import c4.curios.api.inventory.CurioSlotInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CuriosAPI {

    private static Map<String, CurioSlotInfo> idToSlot = new HashMap<>();
    private static Map<String, ResourceLocation> idToResource = new HashMap<>();
    private static Map<String, TextureAtlasSprite> idToSprite = new HashMap<>();

    public static boolean registerCurioSlot(@Nonnull String identifier) {
        return registerCurioSlot(identifier, null);
    }

    public static boolean registerCurioSlot(@Nonnull String identifier, @Nullable ResourceLocation overlay) {

        identifier = identifier.toLowerCase();

        if (identifier.isEmpty()) {
            return false;
        }

        boolean addNewOverlay = overlay != null && !idToResource.containsKey(identifier);

        if (idToSlot.containsKey(identifier)) {
            CurioSlotInfo existingInfo = idToSlot.get(identifier);
            if (existingInfo.getSlotOverlay() != null) {
                overlay = existingInfo.getSlotOverlay();
                addNewOverlay = false;
            }
        }

        if (addNewOverlay) {
            idToResource.put(identifier, overlay);
        }

        CurioSlotInfo slot = new CurioSlotInfo(identifier, overlay);
        idToSlot.put(identifier, slot);
        return true;
    }

    public static List<CurioSlotInfo> getSlotList() {
        return ImmutableList.copyOf(idToSlot.values());
    }

    public static Map<String, ResourceLocation> getResourceMap() {
        return ImmutableMap.copyOf(idToResource);
    }

    @Nullable
    public static CurioSlotInfo getSlotFromID(String identifier) {
        return idToSlot.get(identifier);
    }

    public static void registerSpriteToID(String identifier, TextureAtlasSprite sprite) {
        if (idToSprite.get(identifier) != null) {
            return;
        }
        idToSprite.put(identifier, sprite);
    }

    @Nullable
    public static TextureAtlasSprite getSpriteFromID(String identifier) {
        return idToSprite.get(identifier);
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
