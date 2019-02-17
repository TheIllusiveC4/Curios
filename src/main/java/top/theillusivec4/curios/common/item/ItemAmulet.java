package top.theillusivec4.curios.common.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.capability.CapCurioItem;
import top.theillusivec4.curios.api.capability.ICurio;

public class ItemAmulet extends Item implements ICurio {

    public ItemAmulet() {
        super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).defaultMaxDamage(0));
        this.setRegistryName(Curios.MODID, "amulet");
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound unused) {
        return CapCurioItem.createProvider(new ICurio() {

            @Override
            public void onCurioTick(ItemStack stack, EntityLivingBase entityLivingBase) {

                if (!entityLivingBase.getEntityWorld().isRemote && entityLivingBase.ticksExisted % 40 == 0) {
                    entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 80, 0, true, true));
                }
            }
        });
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
