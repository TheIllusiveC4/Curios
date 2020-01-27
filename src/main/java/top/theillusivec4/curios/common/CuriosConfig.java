/*
 * Copyright (c) 2018-2020 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.curios.common;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import top.theillusivec4.curios.Curios;

public class CuriosConfig {

  public static final ForgeConfigSpec clientSpec;
  public static final Client CLIENT;
  public static final ForgeConfigSpec commonSpec;
  public static final Common COMMON;
  private static final String CONFIG_PREFIX = "gui." + Curios.MODID + ".config.";

  static {
    final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder()
        .configure(Client::new);
    clientSpec = specPair.getRight();
    CLIENT = specPair.getLeft();
  }

  static {
    final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder()
        .configure(Common::new);
    commonSpec = specPair.getRight();
    COMMON = specPair.getLeft();
  }

  public static class Common {

    public final ForgeConfigSpec.ConfigValue<List<String>> disabledCurios;
    public final ForgeConfigSpec.ConfigValue<List<String>> createCurios;

    Common(ForgeConfigSpec.Builder builder) {

      builder.push("common");

      disabledCurios = builder.comment("List of curio types to disable by default")
          .translation(CONFIG_PREFIX + "disabledCurios").worldRestart()
          .define("disabledCurios", Lists.newArrayList());

      createCurios = builder.comment("List of curio types to create."
          + "Sizes can be defined by adding a semicolon and the size number (e.g. 'ring;4').")
          .translation(CONFIG_PREFIX + "createCurios").worldRestart()
          .define("createCurios", Lists.newArrayList());

      builder.pop();
    }
  }

  public static class Client {

    public final ForgeConfigSpec.BooleanValue renderCurios;
    public final ForgeConfigSpec.IntValue buttonXOffset;
    public final ForgeConfigSpec.IntValue buttonYOffset;
    public final ForgeConfigSpec.IntValue creativeButtonXOffset;
    public final ForgeConfigSpec.IntValue creativeButtonYOffset;
    public final ForgeConfigSpec.EnumValue<ButtonCorner> buttonCorner;

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
      TOP_LEFT(26, -75, 73, -62),
      TOP_RIGHT(61, -75, 95, -62),
      BOTTOM_LEFT(26, -20, 73, -29),
      BOTTOM_RIGHT(61, -20, 95, -29);

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
