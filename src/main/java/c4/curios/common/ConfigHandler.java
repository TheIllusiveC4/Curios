package c4.curios.common;

import c4.curios.Curios;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.*;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Curios.MODID)
public class ConfigHandler {

    @Name("Render Curios")
    @Comment("Set to true to enable rendering curios")
    public static boolean renderCurios = true;

    @Name("Disabled Curios")
    @Comment("List of curio types to disable by default")
    @RequiresMcRestart
    public static String[] disabledCurios = new String[]{};

    @Mod.EventBusSubscriber(modid = Curios.MODID)
    private static class ConfigEventHandler {

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent evt) {
            if (evt.getModID().equals(Curios.MODID)) {
                ConfigManager.sync(Curios.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
