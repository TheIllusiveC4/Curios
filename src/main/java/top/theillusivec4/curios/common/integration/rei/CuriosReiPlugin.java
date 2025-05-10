package top.theillusivec4.curios.common.integration.rei;

import java.util.ArrayList;
import java.util.List;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.renderer.Rect2i;
import top.theillusivec4.curios.client.screen.CuriosScreen;
import top.theillusivec4.curios.common.integration.CuriosExclusionAreas;

@REIPluginClient
public class CuriosReiPlugin implements REIClientPlugin {

  @Override
  public void registerExclusionZones(ExclusionZones zones) {
    zones.register(CuriosScreen.class, screen -> {
      List<Rectangle> rectangles = new ArrayList<>();

      for (Rect2i rect2i : CuriosExclusionAreas.create(screen)) {
        rectangles.add(
            new Rectangle(rect2i.getX(), rect2i.getY(), rect2i.getWidth(), rect2i.getHeight()));
      }
      return rectangles;
    });
  }
}
