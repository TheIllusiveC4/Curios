package top.theillusivec4.curios.common.util;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import top.theillusivec4.curios.Curios;

/**
 * This should be triggered whenever player successfully equips any item in their curios slot. In
 * theory, the item may not necessarily be valid for slot or have ICurio capability attached to it
 * at all, but that is mostly unimportant under normal circumstances.
 * <p>
 * Current implementation allows to perform item and location tests in criteria.
 */

public class EquipCurioTrigger extends SimpleCriterionTrigger<EquipCurioTrigger.Instance> {

  public static final ResourceLocation ID = new ResourceLocation(Curios.MODID, "equip_curio");
  public static final EquipCurioTrigger INSTANCE = new EquipCurioTrigger();

  private EquipCurioTrigger() {
  }

  @Nonnull
  @Override
  public ResourceLocation getId() {
    return ID;
  }

  @Nonnull
  @Override
  public EquipCurioTrigger.Instance createInstance(@Nonnull JsonObject json,
                                                       @Nonnull
                                                           EntityPredicate.Composite playerPred,
                                                       @Nonnull DeserializationContext conditions) {
    return new EquipCurioTrigger.Instance(playerPred, ItemPredicate.fromJson(json.get("item")),
        LocationPredicate.fromJson(json.get("location")));
  }

  public void trigger(ServerPlayer player, ItemStack stack, ServerLevel world, double x,
                      double y, double z) {
    this.trigger(player, instance -> instance.test(stack, world, x, y, z));
  }

  static class Instance extends AbstractCriterionTriggerInstance {

    private final ItemPredicate item;
    private final LocationPredicate location;

    Instance(EntityPredicate.Composite playerPred, ItemPredicate count,
             LocationPredicate indexPos) {
      super(ID, playerPred);
      this.item = count;
      this.location = indexPos;
    }

    @Nonnull
    @Override
    public ResourceLocation getCriterion() {
      return ID;
    }

    boolean test(ItemStack stack, ServerLevel world, double x, double y, double z) {
      return this.item.matches(stack) && this.location.matches(world, x, y, z);
    }
  }
}