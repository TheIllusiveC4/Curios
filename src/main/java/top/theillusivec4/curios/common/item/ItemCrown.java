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

public class ItemCrown extends Item {

  private static final ResourceLocation CROWN_TEXTURE = new ResourceLocation(Curios.MODID,
      "textures/entity/crown.png");

  public ItemCrown() {

    super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).defaultMaxDamage(2000));
    this.setRegistryName(Curios.MODID, "crown");
  }

  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT unused) {

    return CapCurioItem.createProvider(new ICurio() {

      private Object model;

      @Override
      public void onCurioTick(String identifier, int index, LivingEntity livingEntity) {

        if (!livingEntity.getEntityWorld().isRemote && livingEntity.ticksExisted % 20 == 0) {
          livingEntity
              .addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, 300, 44, true, true));
          stack.damageItem(1, livingEntity,
              damager -> CuriosAPI.onBrokenCurio(identifier, index, damager));
        }
      }

      @Override
      public void onUnequipped(String identifier, LivingEntity livingEntity) {

        EffectInstance effect = livingEntity.getActivePotionEffect(Effects.NIGHT_VISION);

        if (effect != null && effect.getAmplifier() == 44) {
          livingEntity.removePotionEffect(Effects.NIGHT_VISION);
        }
      }

      @Override
      public boolean hasRender(String identifier, LivingEntity livingEntity) {

        return true;
      }

      @Override
      public void doRender(String identifier, LivingEntity livingEntity, float limbSwing,
          float limbSwingAmount, float partialTicks, float ageInTicks,
          float netHeadYaw, float headPitch, float scale) {

        Minecraft.getInstance().getTextureManager().bindTexture(CROWN_TEXTURE);

        if (!(this.model instanceof ModelCrown)) {
          model = new ModelCrown();
        }

        ModelCrown crown = (ModelCrown) this.model;
        ICurio.RenderHelper.followHeadRotations(livingEntity, crown.crown);
        crown.render(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
            headPitch, scale);
      }
    });
  }

  @Override
  public boolean hasEffect(ItemStack stack) {

    return true;
  }
}
