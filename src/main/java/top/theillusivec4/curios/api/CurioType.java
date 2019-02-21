package top.theillusivec4.curios.api;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CurioType {

    private final String identifier;
    private ResourceLocation icon;
    private int size;
    private boolean isEnabled;
    private boolean isHidden;

    public CurioType(String identifier) {
        this.identifier = identifier;
        this.size = 1;
        this.isEnabled = true;
        this.isHidden = false;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTranslationKey() {
        return "curios.identifier." + identifier;
    }

    @OnlyIn(Dist.CLIENT)
    public String getFormattedName() {

        if (!I18n.hasKey(getTranslationKey())) {
            return identifier.substring(0, 1).toUpperCase() + identifier.substring(1);
        }
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

    public boolean isHidden() { return isHidden; }

    public final CurioType icon(@Nonnull ResourceLocation icon) {
        this.icon = icon;
        return this;
    }

    public final CurioType defaultSize(int size) {
        this.size = size;
        return this;
    }

    public final CurioType enabled(boolean enabled) {
        this.isEnabled = enabled;
        return this;
    }

    public final CurioType hide(boolean hide) {
        this.isHidden = hide;
        return this;
    }
}
