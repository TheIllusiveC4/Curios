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

package top.theillusivec4.curios.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotPredicate;

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
  public Codec<TriggerInstance> codec() {
    return TriggerInstance.CODEC;
  }

  public void trigger(ServerPlayer serverPlayer, ItemStack stack) {
    LootParams lootparams = new LootParams.Builder(serverPlayer.serverLevel())
        .withParameter(LootContextParams.ORIGIN, serverPlayer.blockPosition().getCenter())
        .withParameter(LootContextParams.THIS_ENTITY, serverPlayer)
        .withParameter(LootContextParams.BLOCK_STATE, serverPlayer.getBlockStateOn())
        .withParameter(LootContextParams.TOOL, stack)
        .create(LootContextParamSets.ADVANCEMENT_LOCATION);
    LootContext lootcontext = new LootContext.Builder(lootparams).create(Optional.empty());
    this.trigger(serverPlayer, instance -> instance.matches(null, stack, lootcontext));
  }

  public void trigger(SlotContext slotContext, ServerPlayer serverPlayer, ItemStack stack) {
    LootParams lootparams = new LootParams.Builder(serverPlayer.serverLevel())
        .withParameter(LootContextParams.ORIGIN, serverPlayer.blockPosition().getCenter())
        .withParameter(LootContextParams.THIS_ENTITY, serverPlayer)
        .withParameter(LootContextParams.BLOCK_STATE, serverPlayer.getBlockStateOn())
        .withParameter(LootContextParams.TOOL, stack)
        .create(LootContextParamSets.ADVANCEMENT_LOCATION);
    LootContext lootcontext = new LootContext.Builder(lootparams).create(Optional.empty());
    this.trigger(serverPlayer, instance -> instance.matches(slotContext, stack, lootcontext));
  }

  public record TriggerInstance(Optional<ContextAwarePredicate> player,
                                Optional<ItemPredicate> item,
                                Optional<LocationPredicate> location,
                                Optional<SlotPredicate> slot)
      implements SimpleInstance {
    public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player")
                    .forGetter(TriggerInstance::player),
                ItemPredicate.CODEC.optionalFieldOf("item")
                    .forGetter(TriggerInstance::item),
                LocationPredicate.CODEC.optionalFieldOf("location").forGetter(
                    TriggerInstance::location),
                SlotPredicate.CODEC.optionalFieldOf("curios:slot")
                    .forGetter(TriggerInstance::slot)
            )
            .apply(instance, TriggerInstance::new)
    );

    public boolean matches(SlotContext slotContext, ItemStack stack, LootContext lootContext) {
      Vec3 vec3 = lootContext.getParameter(LootContextParams.ORIGIN);

      if (slotContext != null &&
          this.slot().map(slotPredicate -> !slotPredicate.matches(slotContext)).orElse(false)) {
        return false;
      }

      if (this.location.isEmpty() ||
          this.location.get().matches(lootContext.getLevel(), vec3.x, vec3.y, vec3.z)) {
        return this.item.isEmpty() || this.item.get().test(stack);
      }
      return false;
    }
  }
}