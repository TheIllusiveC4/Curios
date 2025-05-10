/*
 * Copyright (c) 2018-2024 C4
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
 *
 */

package top.theillusivec4.curios.config;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosResources;

public class CuriosConfig {

  public static final ModConfigSpec SERVER_SPEC;
  public static final Server SERVER;
  public static final ModConfigSpec COMMON_SPEC;
  public static final Common COMMON;
  private static final String CONFIG_PREFIX = "gui." + CuriosResources.MOD_ID + ".config.";

  static {
    final Pair<Server, ModConfigSpec> specPair = new ModConfigSpec.Builder()
        .configure(Server::new);
    SERVER_SPEC = specPair.getRight();
    SERVER = specPair.getLeft();
    final Pair<Common, ModConfigSpec> cspecPair = new ModConfigSpec.Builder()
        .configure(Common::new);
    COMMON_SPEC = cspecPair.getRight();
    COMMON = cspecPair.getLeft();
  }

  public static class Common {

    public ModConfigSpec.ConfigValue<List<? extends String>> slots;

    public Common(ModConfigSpec.Builder builder) {
      slots = builder.comment("""
              List of slots to create or modify.
              See documentation for syntax: https://docs.illusivesoulworks.com/curios/configuration#slot-configuration
              """)
          .translation(CONFIG_PREFIX + "slots")
          .defineList("slots", List.of(), s -> s instanceof String);

      builder.build();
    }
  }

  public static class Server {

    public ModConfigSpec.EnumValue<KeepCurios> keepCurios;
    public ModConfigSpec.IntValue minimumColumns;
    public ModConfigSpec.IntValue maxSlotsPerPage;

    public Server(ModConfigSpec.Builder builder) {
      keepCurios = builder.comment("""
              Sets behavior for keeping Curios items on death.
              ON - Curios items are kept on death
              DEFAULT - Curios items follow the keepInventory gamerule
              OFF - Curios items are dropped on death""")
          .translation(CONFIG_PREFIX + "keepCurios").defineEnum("keepCurios", KeepCurios.DEFAULT);

      builder.push("menu");

      minimumColumns = builder.comment("The minimum number of columns for the Curios menu.")
          .translation(CONFIG_PREFIX + "minimumColumns").defineInRange("minimumColumns", 1, 1, 8);

      maxSlotsPerPage = builder.comment("The maximum number of slots per page of the Curios menu.")
          .translation(CONFIG_PREFIX + "maxSlotsPerPage")
          .defineInRange("maxSlotsPerPage", 48, 1, 48);

      builder.pop();
      builder.build();
    }
  }

  public enum KeepCurios {
    ON,
    DEFAULT,
    OFF
  }
}
