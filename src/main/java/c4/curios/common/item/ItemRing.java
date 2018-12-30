package c4.curios.common.item;

import c4.curios.Curios;
import c4.curios.api.capability.ICurio;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemRing extends Item implements ICurio {

    private static final UUID uuid = UUID.fromString("8b7c8fcd-89bc-4794-8bb9-eddeb32753a5");
    private static final UUID uuid2 = UUID.fromString("38faf191-bf78-4654-b349-cc1f4f1143bf");

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
    public Multimap<String, AttributeModifier> getAttributeModifiers(String identifier, ItemStack stack) {
        Multimap<String, AttributeModifier> atts = HashMultimap.create();

        if (getCurioSlots(stack).contains(identifier)) {
            atts.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(uuid, "Speed bonus", 2, 2));
            atts.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(uuid2, "Armor bonus", 2, 0));
        }
        return atts;
    }

    @Override
    public List<String> getCurioSlots(ItemStack stack) {
        return Arrays.asList("ring");
    }
}
