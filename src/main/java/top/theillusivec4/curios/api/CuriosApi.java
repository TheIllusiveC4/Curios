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

package top.theillusivec4.curios.api;

import top.theillusivec4.curios.api.type.util.ICuriosClient;
import top.theillusivec4.curios.api.type.util.ICuriosHelper;
import top.theillusivec4.curios.api.type.util.ICuriosServer;

public final class CuriosApi {

  public static final String MODID = "curios";

  private static ICuriosClient clientManager;
  private static ICuriosServer serverManager;
  private static ICuriosHelper curiosHelper;

  public static ICuriosClient getClientManager() {
    return clientManager;
  }

  public static void setClientManager(ICuriosClient manager) {

    if (clientManager == null) {
      clientManager = manager;
    }
  }

  public static ICuriosServer getServerManager() {
    return serverManager;
  }

  public static void setServerManager(ICuriosServer manager) {
    serverManager = manager;
  }

  public static ICuriosHelper getCuriosHelper() {
    return curiosHelper;
  }

  public static void setCuriosHelper(ICuriosHelper helper) {

    if (curiosHelper == null) {
      curiosHelper = helper;
    }
  }
}
