package top.theillusivec4.curios.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.TextureStitchEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypePreset;

public class CuriosClientMod {

  private static final CuriosSpriteListener SPRITE_LISTENER = new CuriosSpriteListener();

  @SuppressWarnings("ConstantConditions")
  public static void init() {
    Minecraft mc = Minecraft.getInstance();

    if (mc != null) {
      ResourceManager manager = mc.getResourceManager();

      if (manager instanceof ReloadableResourceManager reloader) {
        reloader.registerReloadListener(SPRITE_LISTENER);
      }
    }
  }

  public static void stitch(final TextureStitchEvent.Pre evt) {

    if (evt.getAtlas().location() == InventoryMenu.BLOCK_ATLAS) {

      for (SlotTypePreset preset : SlotTypePreset.values()) {
        evt.addSprite(
            new ResourceLocation(CuriosApi.MODID,
                "item/empty_" + preset.getIdentifier() + "_slot"));
      }
      evt.addSprite(new ResourceLocation(CuriosApi.MODID, "item/empty_cosmetic_slot"));
      evt.addSprite(new ResourceLocation(CuriosApi.MODID, "item/empty_curio_slot"));

      for (ResourceLocation sprite : SPRITE_LISTENER.getSprites()) {
        evt.addSprite(sprite);
      }
    }
  }
}
