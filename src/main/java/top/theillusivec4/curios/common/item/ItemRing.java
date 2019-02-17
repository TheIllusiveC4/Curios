package top.theillusivec4.curios.common.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
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

import java.util.UUID;

public class ItemRing extends Item implements ICurio {

    private static final UUID SPEED_UUID = UUID.fromString("8b7c8fcd-89bc-4794-8bb9-eddeb32753a5");
    private static final UUID ARMOR_UUID = UUID.fromString("38faf191-bf78-4654-b349-cc1f4f1143bf");

    public ItemRing() {
        super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).defaultMaxDamage(0));
        this.setRegistryName(Curios.MODID, "ring");
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound unused) {
        return CapCurioItem.createProvider(new ICurio() {

            @Override
            public void onCurioTick(ItemStack stack, EntityLivingBase entityLivingBase) {
                if (!entityLivingBase.getEntityWorld().isRemote && entityLivingBase.ticksExisted % 19 == 0) {
                    entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.HASTE, 20, 0, true, true));
                }
            }

            @Override
            public Multimap<String, AttributeModifier> getAttributeModifiers(String identifier, ItemStack stack) {
                Multimap<String, AttributeModifier> atts = HashMultimap.create();

                if (getCurioTypes(stack).contains(identifier)) {
                    atts.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(SPEED_UUID, "Speed bonus", 2, 2));
                    atts.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_UUID, "Armor bonus", 2, 0));
                }
                return atts;
            }
        });
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
