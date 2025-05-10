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

package top.theillusivec4.curios.api.client;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;

/**
 * Registry class for registering and obtaining curio rendering logic.
 *
 * @deprecated Getters and registration have relocated to utility methods directly inside the
 *     {@link ICurioRenderer} interface and there is no longer a need for a separate class.
 */
@Deprecated(forRemoval = true)
public class CuriosRendererRegistry {

  /**
   * Registers a renderer to an item.
   * <br>
   * This should be called in the FMLClientSetupEvent event
   *
   * @param item     The item to check for
   * @param renderer The supplier renderer to invoke for the item in the registry
   * @see ICurioRenderer
   * @deprecated Replaced by {@link ICurioRenderer#register(Item, Supplier)} for a simpler and
   *     more streamlined registration method.
   */
  @Deprecated(forRemoval = true)
  public static void register(Item item, Supplier<ICurioRenderer> renderer) {
    ICurioRenderer.register(item, renderer);
  }

  /**
   * Returns the renderer associated with the item, or an empty optional if none is found.
   *
   * @param item The item to check for
   * @return An optional renderer value associated with the item
   * @see ICurioRenderer
   * @deprecated Replaced by {@link ICurioRenderer#get(Item)} and various other getter methods for
   *     ease of use without an Optional instance.
   */
  @Deprecated(forRemoval = true)
  public static Optional<ICurioRenderer> getRenderer(Item item) {
    return Optional.ofNullable(ICurioRenderer.getOrNull(item));
  }
}
