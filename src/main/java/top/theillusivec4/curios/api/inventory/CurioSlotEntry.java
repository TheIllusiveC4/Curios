package top.theillusivec4.curios.api.inventory;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CurioSlotEntry {

    private final String identifier;
    private ResourceLocation icon;
    private int size;
    private boolean isEnabled;

    public CurioSlotEntry(String identifier) {
        this.identifier = identifier;
        this.size = 1;
        this.isEnabled = true;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTranslationKey() {
        return "curios.identifier." + identifier;
    }

    @OnlyIn(Dist.CLIENT)
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

    public boolean isEnabled() {
        return isEnabled;
    }

    public final CurioSlotEntry setIcon(@Nonnull ResourceLocation icon) {
        this.icon = icon;
        return this;
    }

    public final CurioSlotEntry setSize(int size) {
        this.size = size;
        return this;
    }

    public final CurioSlotEntry setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        return this;
    }
}
