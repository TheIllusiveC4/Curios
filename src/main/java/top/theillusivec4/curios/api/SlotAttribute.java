package top.theillusivec4.curios.api;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class SlotAttribute extends Attribute {

  private static final Map<String, SlotAttribute> SLOT_ATTRIBUTES = new HashMap<>();

  private final String identifier;

  public static SlotAttribute getOrCreate(String id) {
    return SLOT_ATTRIBUTES.computeIfAbsent(id, SlotAttribute::new);
  }

  protected SlotAttribute(String identifier) {
    super("curios.slot." + identifier, 0);
    this.identifier = identifier;
  }

  public String getIdentifier() {
    return this.identifier;
  }
}
