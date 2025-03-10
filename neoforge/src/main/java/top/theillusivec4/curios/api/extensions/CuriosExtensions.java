package top.theillusivec4.curios.api.extensions;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class CuriosExtensions {

  static final Map<String, ICurioSlotExtension> SLOT_EXTENSIONS =
      new Object2ReferenceOpenHashMap<>();

  static void register(ICurioSlotExtension extension, String[] slotIds) {

    if (slotIds.length == 0) {
      throw new IllegalArgumentException("At least one slot must be provided");
    }
    Objects.requireNonNull(extension, "Slot extension must not be null");

    for (String id : slotIds) {
      Objects.requireNonNull(id, "Slot must not be null");
      ICurioSlotExtension oldExtensions = SLOT_EXTENSIONS.put(id, extension);

      if (oldExtensions != null) {
        throw new IllegalStateException(
            String.format(
                Locale.ROOT,
                "Duplicate slot extensions registration for %s (old: %s, new: %s)",
                id,
                oldExtensions,
                extension));
      }
    }
  }
}
