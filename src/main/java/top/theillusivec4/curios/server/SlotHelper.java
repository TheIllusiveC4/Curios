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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.util.ISlotHelper;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;

public class SlotHelper implements ISlotHelper {

  private Map<String, ISlotType> idToType = new HashMap<>();

  @Override
  public void addSlotType(ISlotType slotType) {
    this.idToType.put(slotType.getIdentifier(), slotType);
  }

  @Override
  public Optional<ISlotType> getSlotType(String identifier) {
    return Optional.ofNullable(this.idToType.get(identifier));
  }

  @Override
  public Collection<ISlotType> getSlotTypes() {
    return Collections.unmodifiableCollection(idToType.values());
  }

  @Override
  public Collection<ISlotType> getSlotTypes(LivingEntity livingEntity) {
    return getSlotTypes();
  }

  @Override
  public SortedMap<ISlotType, ICurioStacksHandler> createSlots() {
    SortedMap<ISlotType, ICurioStacksHandler> curios = new TreeMap<>();
    this.getSlotTypes().forEach(type -> curios.put(type,
        new CurioStacksHandler(null, type.getIdentifier(), type.getSize(), type.isVisible(),
            type.hasCosmetic())));
    return curios;
  }

  @Override
  public SortedMap<ISlotType, ICurioStacksHandler> createSlots(LivingEntity livingEntity) {
    SortedMap<ISlotType, ICurioStacksHandler> curios = new TreeMap<>();
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(
        handler -> this.getSlotTypes().forEach(type -> curios.put(type,
            new CurioStacksHandler(handler, type.getIdentifier(), type.getSize(), type.isVisible(),
                type.hasCosmetic()))));
    return curios;
  }

  @Override
  public Set<String> getSlotTypeIds() {
    return Collections.unmodifiableSet(idToType.keySet());
  }

  @Override
  public int getSlotsForType(@Nonnull final LivingEntity livingEntity, String identifier) {
    return CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).map(
        handler -> handler.getStacksHandler(identifier).map(ICurioStacksHandler::getSlots)
            .orElse(0)).orElse(0);
  }

  @Override
  public void setSlotsForType(String id, final LivingEntity livingEntity, int amount) {
    int difference = amount - getSlotsForType(livingEntity, id);

    if (difference > 0) {
      growSlotType(id, difference, livingEntity);
    } else if (difference < 0) {
      shrinkSlotType(id, Math.abs(difference), livingEntity);
    }
  }

  @Override
  public void growSlotType(String id, final LivingEntity livingEntity) {
    growSlotType(id, 1, livingEntity);
  }

  @Override
  public void growSlotType(String id, int amount, final LivingEntity livingEntity) {
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity)
        .ifPresent(handler -> handler.growSlotType(id, amount));
  }

  @Override
  public void shrinkSlotType(String id, final LivingEntity livingEntity) {
    shrinkSlotType(id, 1, livingEntity);
  }

  @Override
  public void shrinkSlotType(String id, int amount, final LivingEntity livingEntity) {
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity)
        .ifPresent(handler -> handler.shrinkSlotType(id, amount));
  }

  @Override
  public void unlockSlotType(String id, LivingEntity livingEntity) {
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {

      if (handler.getStacksHandler(id).map(stacksHandler -> stacksHandler.getSlots() == 0)
          .orElse(false)) {
        handler.growSlotType(id, 1);
      }
    });
  }

  @Override
  public void lockSlotType(String id, LivingEntity livingEntity) {
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {
      int amount = handler.getStacksHandler(id).map(ICurioStacksHandler::getSlots).orElse(0);

      if (amount > 0) {
        handler.shrinkSlotType(id, amount);
      }
    });
  }
}
