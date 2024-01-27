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
  public static EquipBuilder equip() {
    return new EquipBuilder();
  }

  /**
   * @deprecated Use {@link CuriosTriggers#equip()} to build the needed parameters
   */
  @Deprecated
  @Nonnull
  public static CriterionTriggerInstance equip(ItemPredicate.Builder itemPredicate) {
    return new EmptyInstance();
  }

  /**
   * @deprecated Use {@link CuriosTriggers#equip()} to build the needed parameters
   */
  @Deprecated
  @Nonnull
  public static CriterionTriggerInstance equipAtLocation(ItemPredicate.Builder itemPredicate,
                                                         LocationPredicate.Builder locationPredicate) {
    return new EmptyInstance();
  }

  public static final class EquipBuilder {

    private ItemPredicate.Builder itemPredicate;
    private LocationPredicate.Builder locationPredicate;
    private SlotPredicate.Builder slotPredicate;

    private EquipBuilder() {
    }

    public EquipBuilder withItem(ItemPredicate.Builder builder) {
      this.itemPredicate = builder;
      return this;
    }

    public EquipBuilder withLocation(LocationPredicate.Builder builder) {
      this.locationPredicate = builder;
      return this;
    }

    public EquipBuilder withSlot(SlotPredicate.Builder builder) {
      this.slotPredicate = builder;
      return this;
    }

    public CriterionTriggerInstance build() {
      return new EmptyInstance();
    }
  }

  private static final class EmptyInstance extends AbstractCriterionTriggerInstance {

    public EmptyInstance() {
      super(new ResourceLocation(CuriosApi.MODID, "empty"), EntityPredicate.Composite.ANY);
    }
  }
}
