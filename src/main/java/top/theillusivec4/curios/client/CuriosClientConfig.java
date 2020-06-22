package top.theillusivec4.curios.client;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import org.apache.commons.lang3.tuple.Pair;
import top.theillusivec4.curios.Curios;

public class CuriosClientConfig {

  public static final ForgeConfigSpec CLIENT_SPEC;
  public static final Client CLIENT;
  private static final String CONFIG_PREFIX = "gui." + Curios.MODID + ".config.";

  static {
    final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder()
        .configure(Client::new);
    CLIENT_SPEC = specPair.getRight();
    CLIENT = specPair.getLeft();
  }

  public static class Client {

    public final BooleanValue renderCurios;
    public final IntValue buttonXOffset;
    public final IntValue buttonYOffset;
    public final IntValue creativeButtonXOffset;
    public final IntValue creativeButtonYOffset;
    public final EnumValue<ButtonCorner> buttonCorner;

    Client(ForgeConfigSpec.Builder builder) {

      builder.comment("Client only settings, mostly things related to rendering").push("client");

      renderCurios = builder.comment("Set to true to enable rendering curios")
          .translation(CONFIG_PREFIX + "renderCurios").define("renderCurios", true);
      buttonXOffset = builder.comment("The X-Offset for the Curios GUI button")
          .translation(CONFIG_PREFIX + "buttonXOffset")
          .defineInRange("buttonXOffset", 0, -100, 100);
      buttonYOffset = builder.comment("The Y-Offset for the Curios GUI button")
          .translation(CONFIG_PREFIX + "buttonYOffset")
          .defineInRange("buttonYOffset", 0, -100, 100);
      creativeButtonXOffset = builder.comment("The X-Offset for the Creative Curios GUI button")
          .translation(CONFIG_PREFIX + "creativeButtonXOffset")
          .defineInRange("creativeButtonXOffset", 0, -100, 100);
      creativeButtonYOffset = builder.comment("The Y-Offset for the Creative Curios GUI button")
          .translation(CONFIG_PREFIX + "creativeButtonYOffset")
          .defineInRange("creativeButtonYOffset", 0, -100, 100);
      buttonCorner = builder.comment("The corner for the Curios GUI button")
          .translation(CONFIG_PREFIX + "buttonCorner")
          .defineEnum("buttonCorner", ButtonCorner.TOP_LEFT);

      builder.pop();
    }

    public enum ButtonCorner {
      TOP_LEFT(26, -75, 73, -62), TOP_RIGHT(61, -75, 95, -62), BOTTOM_LEFT(26, -20, 73,
          -29), BOTTOM_RIGHT(61, -20, 95, -29);

      final int xoffset;
      final int yoffset;
      final int creativeXoffset;
      final int creativeYoffset;

      ButtonCorner(int x, int y, int creativeX, int creativeY) {
        xoffset = x;
        yoffset = y;
        creativeXoffset = creativeX;
        creativeYoffset = creativeY;
      }

      public int getXoffset() {
        return xoffset;
      }

      public int getYoffset() {
        return yoffset;
      }

      public int getCreativeXoffset() {
        return creativeXoffset;
      }

      public int getCreativeYoffset() {
        return creativeYoffset;
      }
    }
  }
}
