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

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import top.theillusivec4.curios.common.CuriosConfig.CuriosSettings.CuriosSetting;

public class CuriosConfig {

  public static final ForgeConfigSpec SERVER_SPEC;
  public static final Server SERVER;
  public static List<CuriosSetting> curios = new ArrayList<>();

  static {
    final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder()
        .configure(Server::new);
    SERVER_SPEC = specPair.getRight();
    SERVER = specPair.getLeft();
  }

  public static void transformCurios(CommentedConfig configData) {
    SERVER.curiosSettings = new ObjectConverter().toObject(configData, CuriosSettings::new);
    curios = SERVER.curiosSettings.curiosSettings;
  }

  public static class Server {

    public CuriosSettings curiosSettings;

    public Server(ForgeConfigSpec.Builder builder) {
      builder.comment("List of curio slot type settings")
          .define("curiosSettings", new ArrayList<>());
      builder.build();
    }
  }

  public static class CuriosSettings {

    public List<CuriosSetting> curiosSettings;

    public static class CuriosSetting {

      public String identifier;
      public String icon;
      public Integer priority;
      public Integer size;
      public Boolean visible;
      public Boolean hasCosmetic;
      public Boolean override;
    }
  }
}
