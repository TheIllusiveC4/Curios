package top.theillusivec4.curios.common.integration.emi;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.config.SidebarTheme;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.client.screen.CuriosScreen;

public class CuriosEmiIntegration {

  public static void setup(IEventBus eventBus) {
    NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CuriosEmiIntegration::renderCuriosBg);
  }

  public static void renderCuriosBg(final ContainerScreenEvent.Render.Background evt) {

    if (evt.getContainerScreen() instanceof CuriosScreen curiosScreen
        && EmiConfig.leftSidebarTheme != SidebarTheme.TRANSPARENT) {
      EmiScreenManager.SidebarPanel panel = EmiScreenManager.getPanelFor(SidebarSide.LEFT);

      if (panel != null && panel.isVisible()) {
        Bounds bounds = panel.getBounds();
        int emiRight = bounds.right();
        int curiosLeft = curiosScreen.getGuiLeft() - curiosScreen.panelWidth;

        if (emiRight > curiosLeft) {
          Minecraft mc = Minecraft.getInstance();
          curiosScreen.renderBg(evt.getGuiGraphics(),
                                mc.getDeltaTracker().getGameTimeDeltaPartialTick(false),
                                evt.getMouseX(), evt.getMouseY());
        }
      }
    }
  }
}
