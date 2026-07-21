package top.theillusivec4.curiostest.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import top.theillusivec4.curiostest.CuriosTest;

public final class CuriosTestIds {

  public static final ResourceKey<Item> KNUCKLES = createKey("knuckles");
  public static final ResourceKey<Item> AMULET = createKey("amulet");
  public static final ResourceKey<Item> RING = createKey("ring");
  public static final ResourceKey<Item> CROWN = createKey("crown");

  private static ResourceKey<Item> createKey(String path) {
    return ResourceKey.create(Registries.ITEM,
        Identifier.fromNamespaceAndPath(CuriosTest.MODID, path));
  }
}
