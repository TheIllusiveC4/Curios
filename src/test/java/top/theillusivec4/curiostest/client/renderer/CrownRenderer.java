package top.theillusivec4.curiostest.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curiostest.CuriosTest;
import top.theillusivec4.curiostest.client.CuriosLayerDefinitions;
import top.theillusivec4.curiostest.client.model.CrownModel;

public class CrownRenderer implements ICurioRenderer.HumanoidRender {

  private static final ResourceLocation CROWN_TEXTURE =
      ResourceLocation.fromNamespaceAndPath(CuriosTest.MODID,
                                            "textures/entity/crown.png");
  private final CrownModel<HumanoidRenderState> model;

  public CrownRenderer() {
    this.model = new CrownModel<>(
        Minecraft.getInstance().getEntityModels().bakeLayer(CuriosLayerDefinitions.CROWN));
  }

  @Override
  public HumanoidModel<? extends HumanoidRenderState> getModel(ItemStack stack,
                                                               SlotContext slotContext) {
    return this.model;
  }

  @Override
  public ResourceLocation getModelTexture(ItemStack stack, SlotContext slotContext) {
    return CROWN_TEXTURE;
  }
}
