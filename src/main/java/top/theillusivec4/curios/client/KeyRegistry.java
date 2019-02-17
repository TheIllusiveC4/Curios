package top.theillusivec4.curios.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyRegistry {

    public static KeyBinding openCurios;

    public static void registerKeys() {
        openCurios = registerKeybinding(new KeyBinding("key.curios.open.desc", GLFW.GLFW_KEY_G,
                "key.curios.category"));
    }

    private static KeyBinding registerKeybinding(KeyBinding key) {
        ClientRegistry.registerKeyBinding(key);
        return key;
    }
}
