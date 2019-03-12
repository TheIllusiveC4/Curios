package top.theillusivec4.curios.client.render;

import net.minecraft.client.renderer.entity.model.ModelBase;
import net.minecraft.client.renderer.entity.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCrown extends ModelBase {

    public ModelRenderer crown;

    public ModelCrown() {
        this.textureWidth = 32;
        this.textureHeight = 32;
        this.crown = new ModelRenderer(this, 0, 0);
        this.crown.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.crown.addBox(-3.0F, -12.0F, -3.0F, 6, 4, 6, 0.0F);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.crown.render(scale);
    }
}
