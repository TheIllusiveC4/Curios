package top.theillusivec4.curiostest.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curiostest.CuriosTest;
import top.theillusivec4.curiostest.client.CuriosLayerDefinitions;
import top.theillusivec4.curiostest.client.model.KnucklesModel;

public class KnucklesRenderer implements ICurioRenderer.HumanoidRender {

  private static final ResourceLocation KNUCKLES_TEXTURE =
      new ResourceLocation(CuriosTest.MODID, "textures/entity/knuckles.png");

  private final KnucklesModel model;

  public KnucklesRenderer() {
    this.model =
        new KnucklesModel(
            Minecraft.getInstance().getEntityModels().bakeLayer(CuriosLayerDefinitions.KNUCKLES));
  }

  @Override
  public HumanoidModel<LivingEntity> getModel(ItemStack stack, SlotContext slotContext) {
    return this.model;
  }

  @Override
  public ResourceLocation getModelTexture(ItemStack stack, SlotContext slotContext) {
    return KNUCKLES_TEXTURE;
  }
}
