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

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosConfig {

  public static final ForgeConfigSpec SERVER_SPEC;
  public static final Server SERVER;
  private static final String CONFIG_PREFIX = "gui." + CuriosApi.MODID + ".config.";

  static {
    final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder()
        .configure(Server::new);
    SERVER_SPEC = specPair.getRight();
    SERVER = specPair.getLeft();
  }

  public static class Server {

    public ForgeConfigSpec.EnumValue<KeepCurios> keepCurios;

    public Server(ForgeConfigSpec.Builder builder) {
      keepCurios = builder.comment("""
              Sets behavior for keeping Curios items on death.
              ON - Curios items are kept on death
              DEFAULT - Curios items follow the keepInventory gamerule
              OFF - Curios items are dropped on death""")
          .translation(CONFIG_PREFIX + "keepCurios").defineEnum("keepCurios", KeepCurios.DEFAULT);

      builder.build();
    }
  }

  public enum KeepCurios {
    ON,
    DEFAULT,
    OFF
  }
}
