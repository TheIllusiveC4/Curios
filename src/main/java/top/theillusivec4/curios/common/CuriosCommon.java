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

import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosComponent;

public class CuriosCommon implements ModInitializer {

  public static final String MODID = CuriosApi.MODID;
  public static final Logger LOGGER = LogManager.getLogger();

  private static final boolean DEBUG = false;

  @Override
  public void onInitialize() {
    CuriosApi.setCuriosHelper(new CuriosHelper());
    EntityComponentCallback.event(PlayerEntity.class).register(
        (playerEntity, componentContainer) -> componentContainer
            .put(CuriosComponent.INVENTORY, new CurioInventoryComponent()));
    EntityComponents
        .setRespawnCopyStrategy(CuriosComponent.INVENTORY, RespawnCopyStrategy.INVENTORY);
  }
}
