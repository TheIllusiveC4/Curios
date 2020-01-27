package top.theillusivec4.curios.integration.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.Rectangle2d;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.client.gui.CuriosScreen;

public class CuriosContainerHandler implements IGuiContainerHandler<CuriosScreen> {

  @Override
  @Nonnull
  public List<Rectangle2d> getGuiExtraAreas(CuriosScreen containerScreen) {
    ClientPlayerEntity player = containerScreen.getMinecraft().player;

    if (player != null) {
      return CuriosAPI.getCuriosHandler(containerScreen.getMinecraft().player).map(handler -> {
        List<Rectangle2d> areas = new ArrayList<>();
        int slotCount = handler.getSlots();
        int width = slotCount > 8 ? 42 : 26;
        int height = 7 + slotCount * 18;
        int left = containerScreen.getGuiLeft() - width;
        int top = containerScreen.getGuiTop() + 4;
        areas.add(new Rectangle2d(left, top, width, height));
        return areas;
      }).orElse(Collections.emptyList());
    } else {
      return Collections.emptyList();
    }
  }
}
