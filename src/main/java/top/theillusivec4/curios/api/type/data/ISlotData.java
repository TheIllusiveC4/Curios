package top.theillusivec4.curios.api.type.data;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.crafting.conditions.ICondition;
import top.theillusivec4.curios.api.type.capability.ICurio;

public interface ISlotData {

  ISlotData replace(boolean replace);

  ISlotData order(int order);

  ISlotData size(int size);

  default ISlotData operation(String operation) {
    return this.operation(AttributeModifier.Operation.ADDITION);
  }

  ISlotData useNativeGui(boolean useNativeGui);

  ISlotData addCosmetic(boolean addCosmetic);

  ISlotData renderToggle(boolean renderToggle);

  ISlotData icon(ResourceLocation icon);

  ISlotData dropRule(ICurio.DropRule dropRule);

  ISlotData addCondition(ICondition condition);

  ISlotData addValidator(ResourceLocation resourceLocation);

  JsonObject serialize();

  /**
   * @see #operation(String)
   */
  @Deprecated(forRemoval = true)
  ISlotData operation(AttributeModifier.Operation operation);
}
