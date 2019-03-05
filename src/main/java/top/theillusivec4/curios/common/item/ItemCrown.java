package top.theillusivec4.curios.common.item;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.capability.ICurio;
import top.theillusivec4.curios.client.render.ModelCrown;
import top.theillusivec4.curios.common.capability.CapCurioItem;

public class ItemCrown extends Item implements ICurio {

    public ItemCrown() {
        super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).defaultMaxDamage(0));
        this.setRegistryName(Curios.MODID, "crown");
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound unused) {
        return CapCurioItem.createProvider(new ICurio() {

            private final ResourceLocation CROWN_TEXTURE = new ResourceLocation(Curios.MODID, "textures/entity/crown.png");
            private final ModelCrown crown = new ModelCrown();

            @Override
            public void onCurioTick(String identifier, EntityLivingBase entityLivingBase) {

                if (!entityLivingBase.getEntityWorld().isRemote && entityLivingBase.ticksExisted % 20 == 0) {
                    entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 300, -44, true, true));
                }
            }

            @Override
            public void onUnequipped(String identifier, EntityLivingBase entityLivingBase) {
                PotionEffect effect = entityLivingBase.getActivePotionEffect(MobEffects.NIGHT_VISION);

                if (effect != null && effect.getAmplifier() == -44) {
                    entityLivingBase.removePotionEffect(MobEffects.NIGHT_VISION);
                }
            }

            @Override
            public boolean hasRender(String identifier, EntityLivingBase entityLivingBase) {
                return true;
            }

            @Override
            public void doRender(String identifier, EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
                Minecraft.getInstance().getTextureManager().bindTexture(CROWN_TEXTURE);
                ICurio.RenderHelper.followHeadRotations(entitylivingbaseIn, crown.crown);
                crown.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            }
        });
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
