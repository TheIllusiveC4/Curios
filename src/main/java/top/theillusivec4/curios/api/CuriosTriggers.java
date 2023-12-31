package top.theillusivec4.curios.api;

import javax.annotation.Nonnull;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.resources.ResourceLocation;

public class CuriosTriggers {

  @Nonnull
  public static CriterionTriggerInstance equip(ItemPredicate.Builder itemPredicate) {
    return new EmptyInstance();
  }

  @Nonnull
  public static CriterionTriggerInstance equipAtLocation(ItemPredicate.Builder itemPredicate,
                                                         LocationPredicate.Builder locationPredicate) {
    return new EmptyInstance();
  }

  private static final class EmptyInstance extends AbstractCriterionTriggerInstance {

    public EmptyInstance() {
      super(new ResourceLocation(CuriosApi.MODID, "empty"), EntityPredicate.Composite.ANY);
    }
  }
}
