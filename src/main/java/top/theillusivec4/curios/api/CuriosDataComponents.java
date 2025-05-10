package top.theillusivec4.curios.api;

import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.common.slot.SlotTypePredicate;
import top.theillusivec4.curios.api.internal.CuriosServices;

/**
 * Data components for Curios items and objects.
 */
public final class CuriosDataComponents {

  public static final DataComponentType<CurioAttributeModifiers> ATTRIBUTE_MODIFIERS =
      CuriosServices.REGISTRY.getAttributeModifierComponent();

  /**
   * Gets the data component for curio attribute modifiers, attribute modifiers that are applied
   * when in a specified curio slot in the curio inventory.
   *
   * @param stack The stack containing the data component.
   * @return The data component for curio attribute modifiers on the stack, or an empty instance
   *     if none are found.
   * @see CurioAttributeModifiers
   */
  @Nonnull
  public static CurioAttributeModifiers getCurioAttributeModifiersOrEmpty(ItemStack stack) {
    return stack.getOrDefault(ATTRIBUTE_MODIFIERS, CurioAttributeModifiers.EMPTY);
  }

  /**
   * Updates the data component for curio attribute modifiers, attribute modifiers that are applied
   * when in a specified curio slot in the curio inventory.
   *
   * <p>This will add the specified attribute modifiers from a {@link CurioAttributeModifiers}
   * data component to the existing component on the stack. If none exist, a new instance will be
   * created and then added to the stack with these modifiers added.
   *
   * @param stack     The stack containing the data component.
   * @param modifiers The attribute modifiers to add to the stack.
   * @see CurioAttributeModifiers
   */
  public static void updateCurioAttributeModifiers(ItemStack stack,
                                                   CurioAttributeModifiers modifiers) {
    updateCurioAttributeModifiers(stack, curioAttributeModifiers -> {
      CurioAttributeModifiers result = curioAttributeModifiers;

      for (CurioAttributeModifiers.Entry modifier : modifiers.modifiers()) {
        result = result.withModifierAdded(modifier.attributeHolder(),
                                          modifier.modifier(),
                                          modifier.slotType());
      }
      return result;
    });
  }

  /**
   * Updates the data component for curio attribute modifiers, attribute modifiers that are applied
   * when in a specified curio slot in the curio inventory.
   *
   * <p>This will add the specified attribute modifier for an attribute and matching slot id(s) to
   * a {@link CurioAttributeModifiers} data component on the stack. If none exist, a new instance
   * will be created and then added to the stack with these parameters added as a modifier.
   *
   * @param stack     The stack containing the data component.
   * @param attribute The attribute to apply the attribute modifier on.
   * @param modifier  The attribute modifier to apply.
   * @param slotIds   The slot id(s) that need to match the slot in order to apply the modifier.
   * @see CurioAttributeModifiers
   */
  public static void updateCurioAttributeModifiers(ItemStack stack,
                                                   Holder<Attribute> attribute,
                                                   AttributeModifier modifier,
                                                   String... slotIds) {
    updateCurioAttributeModifiers(stack, curioAttributeModifiers -> {
      CurioAttributeModifiers result = curioAttributeModifiers;
      result = result.withModifierAdded(attribute, modifier,
                                        SlotTypePredicate.builder().withId(slotIds).build());
      return result;
    });
  }

  /**
   * Updates the data component for curio attribute modifiers, attribute modifiers that are applied
   * when in a specified curio slot in the curio inventory.
   *
   * <p>This will apply an updater function on a{@link CurioAttributeModifiers} data component on
   * the stack. If none exist, a new instance will be created and then added to the stack with this
   * function applied to it.
   *
   * @param stack   The stack containing the data component.
   * @param updater The updater function to apply onto the existing data component, or an empty one
   *                if none exist.
   * @see CurioAttributeModifiers
   */
  public static void updateCurioAttributeModifiers(ItemStack stack,
                                                   UnaryOperator<CurioAttributeModifiers> updater) {
    stack.update(ATTRIBUTE_MODIFIERS, CurioAttributeModifiers.EMPTY, updater);
  }
}
