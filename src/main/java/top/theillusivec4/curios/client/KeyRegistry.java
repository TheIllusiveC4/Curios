package top.theillusivec4.curios.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class KeyRegistry {

  public static KeyBinding openCurios;

  public static void registerKeys() {
    openCurios = registerKeybinding(
        new KeyBinding("key.curios.open.desc", GLFW.GLFW_KEY_G, "key.curios.category"));
  }

  private static KeyBinding registerKeybinding(KeyBinding key) {
    KeyBindingHelper.registerKeyBinding(key);
    return key;
  }
}
