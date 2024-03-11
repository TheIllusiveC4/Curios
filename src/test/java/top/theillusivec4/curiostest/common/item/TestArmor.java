package top.theillusivec4.curiostest.common.item;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public class TestArmor extends ArmorItem {

  private static final UUID ARMOR_UUID = UUID.fromString("26f348df-ffb8-48cc-9664-310ac8e2e1cf");

  public TestArmor(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
    super(pMaterial, pType, pProperties);
  }

  @Override
  public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot,
                                                                      ItemStack stack) {
    Multimap<Attribute, AttributeModifier> map = super.getAttributeModifiers(slot, stack);

    if (slot == this.type.getSlot()) {
      map = LinkedHashMultimap.create(map);
      CuriosApi.addSlotModifier(map, "ring", ARMOR_UUID, 1, AttributeModifier.Operation.ADDITION);
      CuriosApi.addSlotModifier(map, "necklace", ARMOR_UUID, -3,
          AttributeModifier.Operation.ADDITION);
    }
    return map;
  }
}
