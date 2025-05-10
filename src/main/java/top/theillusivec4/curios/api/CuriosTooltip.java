package top.theillusivec4.curios.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.internal.CuriosServices;

/**
 * Helper class for building tooltips in a unified style with Curios
 */
@Deprecated(forRemoval = true)
public class CuriosTooltip {

  private final List<Component> content = new ArrayList<>();
  private final Set<String> identifiers = new HashSet<>();
  private ItemStack stack = ItemStack.EMPTY;
  private LivingEntity livingEntity;

  /**
   * Adds the {@link Component} as-is to the tooltip
   */
  @Deprecated(forRemoval = true)
  public CuriosTooltip append(Component component) {
    this.content.add(component);
    return this;
  }

  /**
   * Adds a custom header stylized in gold text
   */
  @Deprecated(forRemoval = true)
  public CuriosTooltip appendHeader(MutableComponent component) {
    return this.append(component.withStyle(ChatFormatting.GOLD));
  }

  /**
   * Adds a localized header for the specified slot identifier stylized in gold text
   */
  @Deprecated(forRemoval = true)
  public CuriosTooltip appendSlotHeader(String identifier) {
    return this.append(
        Component.translatable("curios.modifiers." + identifier).withStyle(ChatFormatting.GOLD));
  }

  /**
   * Adds the {@link MutableComponent} with blue text, signifying an additive effect
   */
  @Deprecated(forRemoval = true)
  public CuriosTooltip appendAdditive(MutableComponent component) {
    return this.append(component.withStyle(ChatFormatting.BLUE));
  }

  /**
   * Adds the {@link MutableComponent} with red text, signifying a subtractive effect
   */
  @Deprecated(forRemoval = true)
  public CuriosTooltip appendSubtractive(MutableComponent component) {
    return this.append(component.withStyle(ChatFormatting.RED));
  }

  /**
   * Adds the {@link MutableComponent} with dark green text, signifying an equaling effect
   */
  @Deprecated(forRemoval = true)
  public CuriosTooltip appendEqual(MutableComponent component) {
    return this.append(component.withStyle(ChatFormatting.DARK_GREEN));
  }

  /**
   * Wraps the entire content of this tooltip with the specified slot identifiers, appending a slot
   * header followed by the content for each slot
   */
  @Deprecated(forRemoval = true)
  public CuriosTooltip forSlots(String... identifiers) {
    this.identifiers.addAll(Arrays.asList(identifiers));
    return this;
  }

  /**
   * Wraps the entire content of this tooltip with the slot identifiers matching the
   * {@link ItemStack}, appending a slot header followed by the content for each slot type found
   */
  @Deprecated(forRemoval = true)
  public CuriosTooltip forSlots(ItemStack stack) {
    this.stack = stack;
    return this;
  }

  /**
   * Wraps the entire content of this tooltip with the slot identifiers matching the
   * {@link ItemStack} and {@link LivingEntity}, appending a slot header followed by the content for
   * each slot type found for that entity
   */
  @Deprecated(forRemoval = true)
  public CuriosTooltip forSlots(ItemStack stack, LivingEntity livingEntity) {
    this.stack = stack;
    this.livingEntity = livingEntity;
    return this;
  }

  /**
   * @return The finalized tooltip as a list of components
   */
  @Deprecated(forRemoval = true)
  public List<Component> build() {
    List<Component> result = new ArrayList<>();
    Set<String> ids = new TreeSet<>();

    if (!this.identifiers.isEmpty()) {
      ids.addAll(this.identifiers);
    } else if (!this.stack.isEmpty()) {
      Map<String, ISlotType> map = CuriosSlotTypes.getItemSlotTypes(this.stack, this.livingEntity);
      ids.addAll(map.keySet());
    }

    for (String identifier : ids) {
      result.add(Component.empty());
      result.add(Component.translatable("curios.modifiers." + identifier)
          .withStyle(ChatFormatting.GOLD));
      result.addAll(this.content);
    }
    return result;
  }
}
