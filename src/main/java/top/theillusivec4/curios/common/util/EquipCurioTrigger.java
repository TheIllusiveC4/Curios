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