package top.theillusivec4.curiostest.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curiostest.CuriosTest;
import top.theillusivec4.curiostest.client.CuriosLayerDefinitions;
import top.theillusivec4.curiostest.client.model.CrownModel;

public class CrownRenderer<L extends LivingEntity>
    implements ICurioRenderer.ModelRender<CrownModel<L>> {

  private static final ResourceLocation CROWN_TEXTURE =
      new ResourceLocation(CuriosTest.MODID, "textures/entity/crown.png");
  private final CrownModel<L> model;

  public CrownRenderer() {
    this.model =
        new CrownModel<>(
            Minecraft.getInstance().getEntityModels().bakeLayer(CuriosLayerDefinitions.CROWN));
  }

  @Override
  public void prepareModel(
      ItemStack stack,
      SlotContext slotContext,
      PoseStack poseStack,
      RenderLayerParent<LivingEntity, EntityModel<LivingEntity>> renderLayerParent,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch) {
    ICurioRenderer.followHeadRotations(slotContext.entity(), this.model.crown);
  }

  @Override
  public CrownModel<L> getModel(ItemStack stack, SlotContext slotContext) {
    return this.model;
  }

  @Override
  public ResourceLocation getModelTexture(ItemStack stack, SlotContext slotContext) {
    return CROWN_TEXTURE;
  }
}
