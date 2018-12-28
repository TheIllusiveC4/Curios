package c4.curios.api.inventory;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CurioSlotEntry {

    private final String identifier;
    private ResourceLocation icon;
    private int size;

    public CurioSlotEntry(String identifier) {
        this.identifier = identifier;
        this.size = 1;
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
    public ResourceLocation getIcon() {
        return icon;
    }

    public int getSize() {
        return this.size;
    }

    public final CurioSlotEntry icon(@Nonnull ResourceLocation icon) {
        this.icon = icon;
        return this;
    }

    public final CurioSlotEntry size(int size) {
        this.size = size;
        return this;
    }
}
