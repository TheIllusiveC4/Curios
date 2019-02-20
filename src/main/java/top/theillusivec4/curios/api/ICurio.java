package top.theillusivec4.curios.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
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

    /**
     * Compares the current ItemStack and the previous ItemStack in the slot to detect any changes and returns true if
     * the change should be synced to all tracking clients.
     * Note that this check occurs every tick so implementations need to code their own timers for other intervals.
     * @param stack             The current ItemStack in the slot
     * @param previousStack     The previous ItemStack in the slot
     * @param identifier        The identifier of the {@link CurioType} of the slot
     * @param entityLivingBase  The EntityLivingBase that is wearing the ItemStack
     * @return  True to curios the ItemStack change to all tracking clients, false to do nothing
     */
    default boolean shouldSyncToTracking(ItemStack stack, ItemStack previousStack, String identifier, EntityLivingBase entityLivingBase) {
        return hasRender(stack, identifier, entityLivingBase);
    }

    /**
     * Determines if the ItemStack has rendering
     * @param stack             The current ItemStack in the slot
     * @param identifier        The identifier of the {@link CurioType} of the slot
     * @param entityLivingBase  The EntityLivingBase that is wearing the ItemStack
     * @return  True if the ItemStack has rendering, false if it does not
     */
    default boolean hasRender(ItemStack stack, String identifier, EntityLivingBase entityLivingBase) { return false; }

    /**
     * Performs rendering of the ItemStack if {@link ICurio#hasRender(ItemStack, String, EntityLivingBase)} returns true
     * @param stack                 The current ItemStack in the slot
     * @param identifier            The identifier of the {@link CurioType} of the slot
     * @param entitylivingbaseIn    The EntityLivingBase that is wearing the ItemStack
     */
    @OnlyIn(Dist.CLIENT)
    default void doRender(ItemStack stack, String identifier, EntityLivingBase entitylivingbaseIn, float limbSwing,
                          float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
                          float scale) {}
}
