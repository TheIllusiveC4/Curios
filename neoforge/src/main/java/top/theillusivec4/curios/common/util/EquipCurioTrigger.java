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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

/**
 * This should be triggered whenever player successfully equips any item in their curios slot. In
 * theory, the item may not necessarily be valid for slot or have ICurio capability attached to it
 * at all, but that is mostly unimportant under normal circumstances.
 * <p>
 * Current implementation allows to perform item and location tests in criteria.
 */

public class EquipCurioTrigger extends SimpleCriterionTrigger<EquipCurioTrigger.TriggerInstance> {

  public static final EquipCurioTrigger INSTANCE = new EquipCurioTrigger();

  @Nonnull
  @Override
  public Codec<EquipCurioTrigger.TriggerInstance> codec() {
    return EquipCurioTrigger.TriggerInstance.CODEC;
  }

  public void trigger(ServerPlayer serverPlayer, ItemStack stack) {
    this.trigger(serverPlayer, instance -> instance.matches(stack));
  }

  public record TriggerInstance(Optional<ContextAwarePredicate> player,
                                Optional<ItemPredicate> item)
      implements SimpleCriterionTrigger.SimpleInstance {
    public static final Codec<EquipCurioTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
        p_311432_ -> p_311432_.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player")
                    .forGetter(EquipCurioTrigger.TriggerInstance::player),
                ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item")
                    .forGetter(EquipCurioTrigger.TriggerInstance::item)
            )
            .apply(p_311432_, EquipCurioTrigger.TriggerInstance::new)
    );

    public boolean matches(ItemStack stack) {
      return this.item.isEmpty() || this.item.get().matches(stack);
    }
  }
}