package top.theillusivec4.curios.client;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.client.render.CuriosRenderComponents;
import top.theillusivec4.curios.client.screen.CuriosScreen;
import top.theillusivec4.curios.common.CuriosRegistry;
import top.theillusivec4.curios.common.CuriosNetwork;

public class CuriosClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    CuriosApi.setIconHelper(new IconHelper());
    KeyRegistry.registerKeys();
    ClientTickCallback.EVENT.register(client -> {

      if (KeyRegistry.openCurios.wasPressed()) {
        ClientSidePacketRegistry.INSTANCE
            .sendToServer(CuriosNetwork.OPEN_CURIOS, new PacketByteBuf(Unpooled.buffer()));
      }
    });
    ScreenRegistry.register(CuriosRegistry.CURIOS_SCREENHANDLER, CuriosScreen::new);
    ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register(((spriteAtlasTexture, registry) -> {
      for (SlotTypePreset preset : SlotTypePreset.values()) {
        registry.register(new Identifier(CuriosApi.MODID, "item/empty_" + preset.getIdentifier() + "_slot"));
      }
      registry.register(new Identifier(CuriosApi.MODID, "item/empty_cosmetic_slot"));
      registry.register(new Identifier(CuriosApi.MODID, "item/empty_curio_slot"));
    }));
    CuriosRenderComponents.register();
    CuriosClientNetwork.registerPackets();
  }
}
