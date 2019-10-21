package top.theillusivec4.curios.common.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
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
      public void doRender(String identifier, LivingEntity livingEntity, float limbSwing,
          float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
          float headPitch, float scale) {

        Minecraft.getInstance().getTextureManager().bindTexture(KNUCKLES_TEXTURE);

        if (!(this.model instanceof KnucklesModel)) {
          model = new KnucklesModel();
        }

        KnucklesModel knuckles = (KnucklesModel) this.model;
        ICurio.RenderHelper.followBodyRotations(livingEntity, knuckles);
        knuckles.setLivingAnimations(livingEntity, limbSwing, limbSwingAmount, partialTicks);
        knuckles.setRotationAngles(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
            headPitch, scale);
        knuckles.render(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
            scale);
      }
    });
  }

  @Override
  public boolean hasEffect(ItemStack stack) {

    return true;
  }
}
