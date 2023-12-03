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

package top.theillusivec4.curios.common.util;

import com.google.gson.JsonObject;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * This should be triggered whenever player successfully equips any item in their curios slot. In
 * theory, the item may not necessarily be valid for slot or have ICurio capability attached to it
 * at all, but that is mostly unimportant under normal circumstances.
 * <p>
 * Current implementation allows to perform item and location tests in criteria.
 */

public class EquipCurioTrigger extends SimpleCriterionTrigger<EquipCurioTrigger.Instance> {

  public static final EquipCurioTrigger INSTANCE = new EquipCurioTrigger();

  @Nonnull
  @Override
  protected Instance createInstance(@Nonnull JsonObject pJson,
                                    @Nonnull Optional<ContextAwarePredicate> p_297533_,
                                    @Nonnull DeserializationContext pDeserializationContext) {
    return new EquipCurioTrigger.Instance(p_297533_, ItemPredicate.fromJson(pJson.get("item")),
        LocationPredicate.fromJson(pJson.get("location")));
  }

  public void trigger(ServerPlayer player, ItemStack stack, ServerLevel world, double x,
                      double y, double z) {
    this.trigger(player, instance -> instance.test(stack, world, x, y, z));
  }

  static class Instance extends AbstractCriterionTriggerInstance {

    private final Optional<ItemPredicate> item;
    private final Optional<LocationPredicate> location;

    Instance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item,
             Optional<LocationPredicate> location) {
      super(player);
      this.item = item;
      this.location = location;
    }

    boolean test(ItemStack stack, ServerLevel world, double x, double y, double z) {

      if (this.item.isPresent() && !this.item.get().matches(stack)) {
        return false;
      }
      return this.location.isEmpty() || this.location.get().matches(world, x, y, z);
    }
  }
}