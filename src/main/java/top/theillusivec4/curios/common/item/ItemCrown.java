package top.theillusivec4.curios.common.item;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.ICurio;
import top.theillusivec4.curios.client.render.ModelCrown;
import top.theillusivec4.curios.common.capability.CapCurioItem;

public class ItemCrown extends Item implements ICurio {

  public ItemCrown() {

    super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).defaultMaxDamage(2000));
    this.setRegistryName(Curios.MODID, "crown");
  }

  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT unused) {

    return CapCurioItem.createProvider(new ICurio() {

      private final ResourceLocation CROWN_TEXTURE =
          new ResourceLocation(Curios.MODID, "textures/entity/crown.png");

      private Object model;

      @Override
      public void onCurioTick(String identifier, int index, LivingEntity entityLivingBase) {

        if (!entityLivingBase.getEntityWorld().isRemote &&
            entityLivingBase.ticksExisted % 20 == 0) {
          entityLivingBase.addPotionEffect(
              new EffectInstance(Effects.NIGHT_VISION, 300, 44, true, true));
          stack.damageItem(20, entityLivingBase,
                           damager -> CuriosAPI.onBrokenCurio.accept(identifier, index, damager));
        }
      }

      @Override
      public void onUnequipped(String identifier, LivingEntity entityLivingBase) {

        EffectInstance effect = entityLivingBase.getActivePotionEffect(Effects.NIGHT_VISION);

        if (effect != null && effect.getAmplifier() == 44) {
          entityLivingBase.removePotionEffect(Effects.NIGHT_VISION);
        }
      }

      @Override
      public boolean hasRender(String identifier, LivingEntity entityLivingBase) {

        return true;
      }

      @Override
      public void doRender(String identifier, LivingEntity entitylivingbaseIn, float limbSwing,
                           float limbSwingAmount, float partialTicks, float ageInTicks,
                           float netHeadYaw, float headPitch, float scale) {

        Minecraft.getInstance().getTextureManager().bindTexture(CROWN_TEXTURE);

        if (!(this.model instanceof ModelCrown)) {
          model = new ModelCrown();
        }
        ModelCrown crown = (ModelCrown) this.model;
        ICurio.RenderHelper.followHeadRotations(entitylivingbaseIn, crown.crown);
        crown.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                     headPitch, scale);
      }
    });
  }

  @Override
  public boolean hasEffect(ItemStack stack) {

    return true;
  }
}
