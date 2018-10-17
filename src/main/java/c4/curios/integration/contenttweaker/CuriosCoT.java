package c4.curios.integration.contenttweaker;

import c4.curios.api.CuriosAPI;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

@ZenClass("mods.curios")
@ZenRegister
@ModOnly("contenttweaker")
public class CuriosCoT {

    @ZenMethod
    public static void register(String identifier) {
        register(identifier, "");
    }

    @ZenMethod
    public static void register(String identifier, String slotOverlay) {

        if (identifier.isEmpty()) {
            throw new IllegalArgumentException("Empty identifier for curio slot");
        } else {
            CuriosAPI.registerCurioSlot(identifier, slotOverlay.isEmpty() ? null : new ResourceLocation(slotOverlay));
        }
    }
}
