package top.theillusivec4.curios.client.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.CuriosHelper;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;
import top.theillusivec4.curios.common.CuriosConfig;

import javax.annotation.Nonnull;
import java.util.SortedMap;

public class LayerCurios implements LayerRenderer<EntityLivingBase> {

    @Override
    public void render(@Nonnull EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        if (!CuriosConfig.CLIENT.renderCurios.get()) {
            return;
        }
        GlStateManager.pushMatrix();
        CuriosHelper.getCuriosHandler(entitylivingbaseIn).ifPresent(handler -> {
            SortedMap<String, CurioStackHandler> curios = handler.getCurioMap();

            for (String id : curios.keySet()) {
                CurioStackHandler stackHandler = curios.get(id);

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);

                    if (!stack.isEmpty()) {
                        CuriosHelper.getCurio(stack).ifPresent(curio -> {
                            if (curio.hasRender(stack, id, entitylivingbaseIn)) {
                                GlStateManager.pushMatrix();
                                GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                                curio.doRender(stack, id, entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks,
                                        ageInTicks, netHeadYaw, headPitch, scale);
                                GlStateManager.popMatrix();
                            }
                        });
                    }
                }
            }
        });
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
