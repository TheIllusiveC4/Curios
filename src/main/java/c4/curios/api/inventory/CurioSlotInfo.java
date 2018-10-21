package c4.curios.api.inventory;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CurioSlotInfo {

    private final String identifier;
    private final ResourceLocation slotOverlay;

    public CurioSlotInfo(String identifier) {
        this(identifier, null);
    }

    public CurioSlotInfo(String identifier, @Nullable ResourceLocation slotOverlay) {
        this.identifier = identifier.toLowerCase();
        this.slotOverlay = slotOverlay;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTranslationKey() {
        return "curios.identifier." + identifier;
    }

    public String getFormattedName() {
        return I18n.format(getTranslationKey());
    }

    @Nullable
    public ResourceLocation getSlotOverlay() {
        return slotOverlay;
    }
}
