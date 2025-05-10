package top.theillusivec4.curios.api.extensions;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.internal.CuriosServices;

/**
 * Additional behavior for curio slots. Registration occurs in
 * {@link RegisterCuriosExtensionsEvent}.
 */
public interface ICurioSlotExtension {

  ICurioSlotExtension DEFAULT = new ICurioSlotExtension() {
  };

  /**
   * Gets the current {@link ICurioSlotExtension} instance associated with a given slot identifier,
   * or a default non-operational instance if none is found.
   *
   * @param id The slot identifier
   * @return The associated slot extension instance
   */
  static ICurioSlotExtension from(String id) {
    ICurioSlotExtension extension = CuriosServices.EXTENSIONS.getSlotExtension(id);
    return extension != null ? extension : DEFAULT;
  }

  /**
   * Gets the {@link ItemStack} to display for a given {@link SlotContext}.
   *
   * <p>This is only fired on the logical client, and will override the rendering for the stack that
   * is normally found in the inventory. This has no effect on the functionality of those stacks,
   * only the rendering of the stack in the slot on the screen.
   *
   * @param slotContext  The slot context for the slot being rendered
   * @param defaultStack The default stack that is to be rendered from the slot's container
   * @return The stack to be rendered in the slot
   */
  default ItemStack getDisplayStack(SlotContext slotContext, ItemStack defaultStack) {
    return defaultStack;
  }

  /**
   * Gets the tooltip to display for a given {@link SlotContext}.
   *
   * <p>This is only fired on the logical client, and will override the default tooltip for the slot
   * which shows its localized name when the slot does not contain an item.
   *
   * <p>This tooltip still follows the normal logic of appearing only when the slot does not contain
   * an item. In those cases, the tooltip behavior will still be delegated to the item.
   *
   * @param slotContext     The slot context for the slot being hovered
   * @param originalTooltip The original slot tooltip
   * @param tooltipFlag     The tooltip flag that has been set on the current client
   * @return The tooltip to be rendered
   */
  default List<Component> getSlotTooltip(SlotContext slotContext, List<Component> originalTooltip,
                                         TooltipFlag tooltipFlag) {
    return originalTooltip;
  }

  /**
   * Gets the tooltip to display for a given {@link SlotContext}.
   *
   * <p>This is only fired on the logical client, and will override the default tooltip for the slot
   * which shows its localized name when the slot does not contain an item.
   *
   * <p>This tooltip still follows the normal logic of appearing only when the slot does not contain
   * an item. In those cases, the tooltip behavior will still be delegated to the item.
   *
   * @param slotContext The slot context for the slot being hovered
   * @param tooltipFlag The tooltip flag that has been set on the current client
   * @return The tooltip to be rendered
   * @deprecated Replaced by {{@link #getSlotTooltip(SlotContext, List, TooltipFlag)}} with an extra
   *     parameter for modifying or replacing the original tooltip.
   */
  @Deprecated(forRemoval = true)
  default List<Component> getSlotTooltip(SlotContext slotContext, TooltipFlag tooltipFlag) {
    return List.of();
  }
}
