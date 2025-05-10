package top.theillusivec4.curios.api.internal.services;

import javax.annotation.Nullable;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.extensions.ICurioSlotExtension;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public interface ICuriosExtensions {

  void registerCurioItem(ICurioItem curio, Item... item);

  @Nullable
  ICurioItem getCurioItem(Item item);

  void registerSlotExtension(ICurioSlotExtension slotExtension, String... id);

  @Nullable
  ICurioSlotExtension getSlotExtension(String id);
}
