package top.theillusivec4.curios.api.extensions;

import javax.annotation.Nonnull;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import top.theillusivec4.curios.api.internal.CuriosServices;

/**
 * Allows registration of new behavior to various game objects used by Curios. Fired during {@link
 * net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent} on the mod-specific event bus.
 */
public class RegisterCuriosExtensionsEvent extends Event implements IModBusEvent {

  /**
   * Registers an {@link ICurioSlotExtension} instance to a list of slot identifiers.
   *
   * <p>A slot identifier cannot be associated with more than one slot extension instance.
   * Attempting to register duplicates will throw an error.
   *
   * @param extension The slot extension instance
   * @param slotIds The list of slot identifiers to be associated with the specified slot extension
   */
  public void registerSlotExtension(@Nonnull ICurioSlotExtension extension, String... slotIds) {
    CuriosServices.EXTENSIONS.registerSlotExtension(extension, slotIds);
  }

  /**
   * Checks if the slot identifier already has a registered slot extension.
   *
   * @param slotId The slot identifier
   * @return True if the slot identifier has a registered slot extension, otherwise false
   */
  public boolean isSlotExtensionRegistered(String slotId) {
    return CuriosServices.EXTENSIONS.getSlotExtension(slotId) != null;
  }
}
