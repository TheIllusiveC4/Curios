/*
 * Copyright (c) 2018-2023 C4
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

package top.theillusivec4.curios.api.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;

public class CuriosRendererRegistry {

  private static final Map<Item, Supplier<ICurioRenderer>> RENDERER_REGISTRY =
      new ConcurrentHashMap<>();
  private static final Map<Item, ICurioRenderer> RENDERERS = new HashMap<>();

  /**
   * Registers a renderer to an item.
   * <br>
   * This should be called in the {@link net.neoforged.fml.event.lifecycle.FMLClientSetupEvent}
   *
   * @param item     The item to check for
   * @param renderer The supplier renderer to invoke for the item in the registry
   */
  public static void register(Item item, Supplier<ICurioRenderer> renderer) {
    RENDERER_REGISTRY.put(item, renderer);
  }

  /**
   * Returns the renderer associated with the item, or an empty optional if none is found.
   *
   * @param item The item to check for
   * @return An optional renderer value associated with the item
   */
  public static Optional<ICurioRenderer> getRenderer(Item item) {
    return Optional.ofNullable(RENDERERS.get(item));
  }

  /**
   * Loads the renderers into the registry. For internal use only.
   * <br>
   * This is called in {@link net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers}
   */
  public static void load() {

    for (Map.Entry<Item, Supplier<ICurioRenderer>> entry : RENDERER_REGISTRY.entrySet()) {
      RENDERERS.put(entry.getKey(), entry.getValue().get());
    }
  }
}
