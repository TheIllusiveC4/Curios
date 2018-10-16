package c4.curios.common.item;

import c4.curios.Curios;
import c4.curios.api.capability.ICurio;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import java.util.Arrays;
import java.util.List;

public class ItemRing extends Item implements ICurio {

    public ItemRing() {
        this.setCreativeTab(CreativeTabs.TOOLS);
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setRegistryName(Curios.MODID, "ring");
        this.setTranslationKey(Curios.MODID + ".ring");
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    public void onCurioTick(ItemStack stack, EntityLivingBase entityLivingBase) {
        if (!entityLivingBase.getEntityWorld().isRemote && entityLivingBase.ticksExisted % 19 == 0) {
            entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.HASTE, 20, 0, true, true));
        }
    }

    @Override
    public List<String> getCurioSlots(ItemStack stack) {
        return Arrays.asList("ring");
    }
}
