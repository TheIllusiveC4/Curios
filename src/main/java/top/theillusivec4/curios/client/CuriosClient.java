package top.theillusivec4.curios.client;

import net.fabricmc.api.ClientModInitializer;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    CuriosApi.setIconHelper(new IconHelper());
    KeyRegistry.registerKeys();
  }
}
