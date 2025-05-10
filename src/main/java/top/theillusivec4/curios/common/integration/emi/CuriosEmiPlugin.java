package top.theillusivec4.curios.common.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.renderer.Rect2i;
import top.theillusivec4.curios.client.screen.CuriosScreen;
import top.theillusivec4.curios.common.integration.CuriosExclusionAreas;

@EmiEntrypoint
public class CuriosEmiPlugin implements EmiPlugin {

  @Override
  public void register(EmiRegistry registry) {
    registry.addExclusionArea(CuriosScreen.class, (screen, consumer) -> {
      for (Rect2i rect2i : CuriosExclusionAreas.create(screen)) {
        consumer.accept(new Bounds(rect2i.getX(), rect2i.getY(), rect2i.getWidth(),
                                   rect2i.getHeight()));
      }
    });
  }
}
