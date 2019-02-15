package c4.curios.client;

import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurio;
import c4.curios.api.capability.ICurioItemHandler;
import c4.curios.common.ConfigHandler;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class LayerCurios implements LayerRenderer<EntityLivingBase> {

    @Override
    public void doRenderLayer(@Nonnull EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        if (!ConfigHandler.renderCurios || entitylivingbaseIn.isPotionActive(MobEffects.INVISIBILITY)) {
            return;
        }
        GlStateManager.pushMatrix();
        ICurioItemHandler handler = CuriosAPI.getCuriosHandler(entitylivingbaseIn);

        if (handler != null) {
            ImmutableMap<String, ItemStackHandler> curios = handler.getCurioMap();

            for (String id : curios.keySet()) {
                ItemStackHandler stackHandler = curios.get(id);

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);

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
