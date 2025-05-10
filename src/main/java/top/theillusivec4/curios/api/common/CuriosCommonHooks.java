package top.theillusivec4.curios.api.common;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

/**
 * Hooks for firing events and other logic in Curios that occurs both client-side and server-side.
 */
public class CuriosCommonHooks {

  /**
   * Fires the event for {@link CurioAttributeModifierEvent} to modify curio attribute modifiers on
   * an ItemStack.
   *
   * @param stack            The ItemStack being calculated for curio attribute modifiers.
   * @param defaultModifiers The default modifiers found on the stack.
   * @return The result of the modifiers after the event has been posted.
   */
  public static CurioAttributeModifiers computeModifiedAttributes(
      ItemStack stack, CurioAttributeModifiers defaultModifiers) {
    CurioAttributeModifierEvent event = new CurioAttributeModifierEvent(stack, defaultModifiers);
    NeoForge.EVENT_BUS.post(event);
    return event.build();
  }
}
