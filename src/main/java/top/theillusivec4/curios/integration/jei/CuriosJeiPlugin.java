package top.theillusivec4.curios.integration.jei;

import javax.annotation.Nonnull;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.util.ResourceLocation;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.client.gui.CuriosScreen;

@JeiPlugin
public class CuriosJeiPlugin implements IModPlugin {

  @Override
  @Nonnull
  public ResourceLocation getPluginUid() {
    return new ResourceLocation(Curios.MODID, "jei");
  }

  @Override
  public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    registration.addGuiContainerHandler(CuriosScreen.class, new CuriosContainerHandler());
  }
}
