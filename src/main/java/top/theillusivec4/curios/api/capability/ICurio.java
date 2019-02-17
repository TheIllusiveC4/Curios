package top.theillusivec4.curios.api.capability;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import top.theillusivec4.curios.api.CuriosAPI;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface ICurio {

    default void onCurioTick(ItemStack stack, EntityLivingBase entityLivingBase) {}

    default void onEquipped(ItemStack stack, EntityLivingBase entityLivingBase) {}

    default void onUnequipped(ItemStack stack, EntityLivingBase entityLivingBase) {}

    default boolean canEquip(ItemStack stack, EntityLivingBase entityLivingBase) {
        return true;
    }

    default boolean canUnequip(ItemStack stack, EntityLivingBase entityLivingBase) {
        return true;
    }

    default Multimap<String, AttributeModifier> getAttributeModifiers(String identifier, ItemStack stack) {
        return HashMultimap.create();
    }

    default Set<String> getCurioTypes(ItemStack stack) {
        return CuriosAPI.getCurioTags(stack.getItem());
    }

    default boolean hasRender(ItemStack stack, EntityLivingBase entityLivingBase) { return false; }

    default void doRender(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks,
                          float ageInTicks, float netHeadYaw, float headPitch, float scale) {}
}
