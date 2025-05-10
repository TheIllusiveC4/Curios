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

package top.theillusivec4.curios.mixin.core;

import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosTriggers;
import top.theillusivec4.curios.common.util.EquipCurioTrigger;

@Mixin(value = CuriosTriggers.class, remap = false)
public class MixinCuriosTriggers {

  @Inject(at = @At("HEAD"), method = "equip(Lnet/minecraft/advancements/critereon/ItemPredicate$Builder;)Lnet/minecraft/advancements/Criterion;", cancellable = true)
  private static void curios$equip(ItemPredicate.Builder itemPredicate,
                                   CallbackInfoReturnable<Criterion<EquipCurioTrigger.TriggerInstance>> cir) {
    cir.setReturnValue(EquipCurioTrigger.INSTANCE.createCriterion(
        new EquipCurioTrigger.TriggerInstance(Optional.empty(), Optional.of(itemPredicate.build()),
            Optional.empty(), Optional.empty())));
  }

  @Inject(at = @At("HEAD"), method = "equipAtLocation", cancellable = true)
  private static void curios$equipAtLocation(ItemPredicate.Builder itemPredicate,
                                             LocationPredicate.Builder locationPredicate,
                                             CallbackInfoReturnable<Criterion<EquipCurioTrigger.TriggerInstance>> cir) {
    cir.setReturnValue(EquipCurioTrigger.INSTANCE.createCriterion(
        new EquipCurioTrigger.TriggerInstance(Optional.empty(), Optional.of(itemPredicate.build()),
            Optional.of(locationPredicate.build()), Optional.empty())));
  }
}
