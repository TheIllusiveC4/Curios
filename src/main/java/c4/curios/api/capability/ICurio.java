package c4.curios.api.capability;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public interface ICurio {

    default List<String> getCurioSlots(ItemStack stack) {
        return new ArrayList<>();
    }

    default void onCurioTick(ItemStack stack, EntityLivingBase entityLivingBase) {}

    default void onEquipped(ItemStack stack, EntityLivingBase entityLivingBase) {}

    default void onUnequipped(ItemStack stack, EntityLivingBase entityLivingBase) {}

    default boolean canEquip(ItemStack stack, EntityLivingBase entityLivingBase) {
        return true;
    }

    default boolean canUnequip(ItemStack stack, EntityLivingBase entityLivingBase) {
        return true;
    }

    default Multimap<String, AttributeModifier> getAttributeModifiers(String slot, ItemStack stack) {
        return HashMultimap.create();
    }

    default boolean hasKeybinding() {
        return false;
    }

    default void onKeybinding(ItemStack stack, EntityLivingBase entityLivingBase) {}

    default void doRender(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks,
                          float ageInTicks, float netHeadYaw, float headPitch, float scale) {}
}
