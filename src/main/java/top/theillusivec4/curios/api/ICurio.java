package top.theillusivec4.curios.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Set;

public interface ICurio {

    default void onCurioTick(ItemStack stack, String identifier, EntityLivingBase entityLivingBase) {}

    default void onEquipped(ItemStack stack, String identifier, EntityLivingBase entityLivingBase) {}

    default void onUnequipped(ItemStack stack, String identifier, EntityLivingBase entityLivingBase) {}

    default boolean canEquip(ItemStack stack, String identifier, EntityLivingBase entityLivingBase) {
        return true;
    }

    default boolean canUnequip(ItemStack stack, String identifier, EntityLivingBase entityLivingBase) {
        return true;
    }

    default Multimap<String, AttributeModifier> getAttributeModifiers(String identifier, ItemStack stack) {
        return HashMultimap.create();
    }

    default Set<String> getCurioTypes(ItemStack stack) {
        return CuriosAPI.getCurioTags(stack.getItem());
    }

    default boolean canRightClickEquip(ItemStack stack) { return false; }

    default void playEquipSound(ItemStack stack, EntityPlayerSP playerSP) {
        playerSP.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f);
    }

    @OnlyIn(Dist.CLIENT)
    default boolean hasRender(ItemStack stack, String identifier, EntityLivingBase entityLivingBase) { return false; }

    @OnlyIn(Dist.CLIENT)
    default void doRender(ItemStack stack, String identifier, EntityLivingBase entitylivingbaseIn, float limbSwing,
                          float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
                          float scale) {}
}
