package top.theillusivec4.curios.common.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.crafting.conditions.ICondition;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.data.ISlotData;

public class SlotData implements ISlotData {

  private Integer order;
  private Integer size;
  private AttributeModifier.Operation operation;
  private Boolean useNativeGui;
  private Boolean hasCosmetic;
  private ResourceLocation icon;
  private ICurio.DropRule dropRule;
  private Boolean renderToggle;
  private Boolean replace;
  private List<ICondition> conditions;

  @Override
  public SlotData replace(boolean replace) {
    this.replace = replace;
    return this;
  }

  @Override
  public SlotData order(int order) {
    this.order = order;
    return this;
  }

  @Override
  public SlotData size(int size) {
    this.size = size;
    return this;
  }

  @Override
  public SlotData operation(AttributeModifier.Operation operation) {
    this.operation = operation;
    return this;
  }

  @Override
  public SlotData useNativeGui(boolean useNativeGui) {
    this.useNativeGui = useNativeGui;
    return this;
  }

  @Override
  public SlotData addCosmetic(boolean addCosmetic) {
    this.hasCosmetic = addCosmetic;
    return this;
  }

  @Override
  public SlotData renderToggle(boolean renderToggle) {
    this.renderToggle = renderToggle;
    return this;
  }

  @Override
  public SlotData icon(ResourceLocation icon) {
    this.icon = icon;
    return this;
  }

  @Override
  public SlotData dropRule(ICurio.DropRule dropRule) {
    this.dropRule = dropRule;
    return this;
  }

  @Override
  public SlotData addCondition(ICondition condition) {

    if (this.conditions == null) {
      this.conditions = new ArrayList<>();
    }
    this.conditions.add(condition);
    return this;
  }

  @Override
  public JsonObject serialize(HolderLookup.Provider provider) {
    JsonObject jsonObject = new JsonObject();

    if (this.replace != null) {
      jsonObject.addProperty("replace", this.replace);
    }

    if (this.order != null) {
      jsonObject.addProperty("order", this.order);
    }

    if (this.size != null) {
      jsonObject.addProperty("size", this.size);
    }

    if (this.operation != null) {
      jsonObject.addProperty("operation", this.operation.toString());
    }

    if (this.useNativeGui != null) {
      jsonObject.addProperty("use_native_gui", this.useNativeGui);
    }

    if (this.hasCosmetic != null) {
      jsonObject.addProperty("add_cosmetic", this.hasCosmetic);
    }

    if (this.icon != null) {
      jsonObject.addProperty("icon", this.icon.toString());
    }

    if (this.dropRule != null) {
      jsonObject.addProperty("drop_rule", this.dropRule.toString());
    }

    if (this.renderToggle != null) {
      jsonObject.addProperty("render_toggle", this.renderToggle);
    }

    if (this.conditions != null) {
      this.conditions.forEach(condition -> jsonObject.add(ICondition.DEFAULT_FIELD,
          ICondition.CODEC.encode(condition, JsonOps.INSTANCE, JsonOps.INSTANCE.empty())
              .getOrThrow(false, JsonSyntaxException::new)));
    }
    return jsonObject;
  }
}
