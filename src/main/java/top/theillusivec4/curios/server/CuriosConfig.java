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

package top.theillusivec4.curios.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

public class CuriosConfig {

  public static Map<String, CurioSetting> curios = new HashMap<>();

  public static void init() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    File file = new File(FabricLoader.getInstance().getConfigDir() + "/curios.json");

    if (!file.exists()) {

      try (Writer writer = new FileWriter(file)) {
        gson.toJson(new HashMap<>(), writer);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    curios.clear();

    try (Reader reader = new FileReader(file)) {
      curios = gson.fromJson(reader, new TypeToken<Map<String, CurioSetting>>() {
      }.getType());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static class CurioSetting {

    public String icon;
    public Integer priority;
    public Integer size;
    public Boolean locked;
    public Boolean visible;
    public Boolean hasCosmetic;
    public Boolean override;
  }
}
