package top.theillusivec4.curios.common.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.type.data.IEntitiesData;

public class EntitiesData implements IEntitiesData {

  private final Set<EntityType<?>> entities = new HashSet<>();
  private final Set<String> slots = new HashSet<>();
  private Boolean replace;
  private List<ICondition> conditions;

  @Override
  public EntitiesData replace(boolean replace) {
    this.replace = replace;
    return this;
  }

  @Override
  public EntitiesData addPlayer() {
    return addEntities(EntityType.PLAYER);
  }

  @Override
  public EntitiesData addEntities(EntityType<?>... entityTypes) {
    this.entities.addAll(Arrays.stream(entityTypes).toList());
    return this;
  }

  @Override
  public EntitiesData addSlots(String... slots) {
    this.slots.addAll(Arrays.stream(slots).toList());
    return this;
  }

  @Override
  public EntitiesData addCondition(ICondition condition) {

    if (this.conditions == null) {
      this.conditions = new ArrayList<>();
    }
    this.conditions.add(condition);
    return this;
  }

  @Override
  public JsonObject serialize() {
    JsonObject jsonObject = new JsonObject();

    if (this.replace != null) {
      jsonObject.addProperty("replace", this.replace);
    }

    if (!this.entities.isEmpty()) {
      JsonArray arr = new JsonArray();
      this.entities.forEach(entityType -> arr.add(
          Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entityType)).toString()));
      jsonObject.add("entities", arr);
    }

    if (!this.slots.isEmpty()) {
      JsonArray arr = new JsonArray();
      this.slots.forEach(arr::add);
      jsonObject.add("slots", arr);
    }

    if (this.conditions != null) {
      jsonObject.add("conditions",
          CraftingHelper.serialize(this.conditions.toArray(ICondition[]::new)));
    }
    return jsonObject;
  }
}
