package top.theillusivec4.curiostest.common.item;

import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import top.theillusivec4.curios.CuriosConstants;
import top.theillusivec4.curios.api.CuriosApi;

public class TestArmor extends Item {

  private static final ResourceLocation ARMOR_ID =
      ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "armor");

  public TestArmor(Properties pProperties) {
    super(pProperties.humanoidArmor(ArmorMaterials.GOLD, ArmorType.CHESTPLATE));
  }

  @Nonnull
  @Override
  public ItemAttributeModifiers getDefaultAttributeModifiers(@Nonnull ItemStack stack) {
    ItemAttributeModifiers modifiers = super.getDefaultAttributeModifiers(stack);
    EquipmentSlot slot = this.getEquipmentSlot(stack);

    if (slot != null) {
      modifiers = CuriosApi.withSlotModifier(modifiers, "ring", ARMOR_ID, 1,
                                             AttributeModifier.Operation.ADD_VALUE,
                                             EquipmentSlotGroup.bySlot(slot));
      modifiers = CuriosApi.withSlotModifier(modifiers, "necklace", ARMOR_ID, -3,
                                             AttributeModifier.Operation.ADD_VALUE,
                                             EquipmentSlotGroup.bySlot(slot));
    }
    return modifiers;
  }
}
