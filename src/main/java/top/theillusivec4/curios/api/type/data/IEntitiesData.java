package top.theillusivec4.curios.api.type.data;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.crafting.conditions.ICondition;

public interface IEntitiesData {

  IEntitiesData replace(boolean replace);

  IEntitiesData addPlayer();

  IEntitiesData addEntities(EntityType<?>... entityTypes);

  IEntitiesData addSlots(String... slots);

  IEntitiesData addCondition(ICondition condition);

  JsonObject serialize();
}
