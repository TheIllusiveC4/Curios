package top.theillusivec4.curios.api;

import javax.annotation.Nonnull;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.resources.ResourceLocation;

public final class CuriosTriggers {

  @Nonnull
  public static CriterionTriggerInstance equip(ItemPredicate.Builder itemPredicate) {
    CuriosApi.apiError();
    return new EmptyInstance();
  }

  @Nonnull
  public static CriterionTriggerInstance equipAtLocation(ItemPredicate.Builder itemPredicate,
                                                         LocationPredicate.Builder locationPredicate) {
    CuriosApi.apiError();
    return new EmptyInstance();
  }

  private static final class EmptyInstance extends AbstractCriterionTriggerInstance {

    public EmptyInstance() {
      super(new ResourceLocation(CuriosApi.MODID, "empty"), ContextAwarePredicate.ANY);
    }
  }
}
