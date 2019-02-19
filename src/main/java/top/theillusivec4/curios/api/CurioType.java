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
    private boolean hideGUI;

    public CurioType(String identifier) {
        this.identifier = identifier;
        this.size = 1;
        this.isEnabled = true;
        this.hideGUI = false;
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

    public boolean isHidden() { return hideGUI; }

    public final CurioType setIcon(@Nonnull ResourceLocation icon) {
        this.icon = icon;
        return this;
    }

    public final CurioType setSize(int size) {
        this.size = size;
        return this;
    }

    public final CurioType setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        return this;
    }

    public final CurioType hideGUI(boolean hideGUI) {
        this.hideGUI = hideGUI;
        return this;
    }
}
