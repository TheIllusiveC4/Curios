package c4.curios.client;

import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurio;
import c4.curios.api.capability.ICurioItemHandler;
import c4.curios.api.inventory.CurioStackHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Map;

public class LayerCurios implements LayerRenderer<EntityLivingBase> {

    @Override
    public void doRenderLayer(@Nonnull EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        if (entitylivingbaseIn.isPotionActive(MobEffects.INVISIBILITY)) {
            return;
        }
        GlStateManager.pushMatrix();
        ICurioItemHandler handler = CuriosAPI.getCuriosHandler(entitylivingbaseIn);

        if (handler != null) {
            Map<String, CurioStackHandler> curios = handler.getCurioMap();

            for (String id : curios.keySet()) {
                CurioStackHandler stackHandler = curios.get(id);

                for (ItemStack stack : stackHandler.getStacks()) {

                    if (!stack.isEmpty()) {
                        ICurio curio = CuriosAPI.getCurio(stack);

                        if (curio != null && curio.hasRender(stack, entitylivingbaseIn)) {
                            curio.doRender(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw,
                                    headPitch, scale);
                        }
                    }
                }
            }
        }
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
