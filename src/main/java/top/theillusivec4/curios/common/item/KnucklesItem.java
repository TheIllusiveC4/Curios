package top.theillusivec4.curios.common.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.UUID;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.ICurio;
import top.theillusivec4.curios.client.render.model.KnucklesModel;
import top.theillusivec4.curios.common.capability.CapCurioItem;

public class KnucklesItem extends Item {

  private static final UUID AD_UUID = UUID.fromString("7ce10414-adcc-4bf2-8804-f5dbd39fadaf");
  private static final ResourceLocation KNUCKLES_TEXTURE = new ResourceLocation(Curios.MODID,
      "textures/entity/knuckles.png");

  public KnucklesItem() {
    super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1));
    this.setRegistryName(Curios.MODID, "knuckles");
  }

  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT unused) {
    return CapCurioItem.createProvider(new ICurio() {

      private Object model;

      @Override
      public Multimap<String, AttributeModifier> getAttributeModifiers(String identifier) {
        Multimap<String, AttributeModifier> atts = HashMultimap.create();

        if (CuriosAPI.getCurioTags(stack.getItem()).contains(identifier)) {
          atts.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
              new AttributeModifier(AD_UUID, "Attack damage bonus", 4,
                  AttributeModifier.Operation.ADDITION));
        }
        return atts;
      }

      @Override
      public boolean hasRender(String identifier, LivingEntity livingEntity) {
        return true;
      }

      @Override
      public void render(String identifier, MatrixStack matrixStack,
          IRenderTypeBuffer renderTypeBuffer, int light, LivingEntity livingEntity, float limbSwing,
          float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
          float headPitch) {

        if (!(this.model instanceof KnucklesModel)) {
          model = new KnucklesModel();
        }

        KnucklesModel knuckles = (KnucklesModel) this.model;
        ICurio.RenderHelper.followBodyRotations(livingEntity, knuckles);
        knuckles.setLivingAnimations(livingEntity, limbSwing, limbSwingAmount, partialTicks);
        knuckles.func_225597_a_(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
            headPitch);
        IVertexBuilder vertexBuilder = ItemRenderer
            .func_229113_a_(renderTypeBuffer, knuckles.func_228282_a_(KNUCKLES_TEXTURE), false,
                stack.hasEffect());
        knuckles
            .func_225598_a_(matrixStack, vertexBuilder, light, OverlayTexture.field_229196_a_, 1.0F,
                1.0F, 1.0F, 1.0F);
      }
    });
  }

  @Override
  public boolean hasEffect(ItemStack stack) {
    return true;
  }
}
