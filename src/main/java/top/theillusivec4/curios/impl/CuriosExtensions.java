package top.theillusivec4.curios.impl;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.extensions.ICurioSlotExtension;
import top.theillusivec4.curios.api.internal.services.ICuriosExtensions;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public final class CuriosExtensions implements ICuriosExtensions {

  static final Map<Item, ICurioItem> REGISTERED_ITEMS =
      Reference2ObjectMaps.synchronize(new Reference2ObjectOpenHashMap<>());
  static final Map<String, ICurioSlotExtension> SLOT_EXTENSIONS =
      Object2ReferenceMaps.synchronize(new Object2ReferenceOpenHashMap<>());

  @Override
  public void registerCurioItem(ICurioItem curioItem, Item... items) {

    if (items.length == 0) {
      throw new IllegalArgumentException("At least one item must be provided");
    }
    Objects.requireNonNull(curioItem, "Curio item must not be null");

    for (Item item : items) {
      Objects.requireNonNull(item, "Item must not be null");
      REGISTERED_ITEMS.put(item, curioItem);
    }
  }

  @Nullable
  @Override
  public ICurioItem getCurioItem(Item item) {
    return REGISTERED_ITEMS.get(item);
  }

  @Override
  public void registerSlotExtension(ICurioSlotExtension extension, String... slotIds) {

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

  @Nullable
  @Override
  public ICurioSlotExtension getSlotExtension(String id) {
    return SLOT_EXTENSIONS.get(id);
  }
}
