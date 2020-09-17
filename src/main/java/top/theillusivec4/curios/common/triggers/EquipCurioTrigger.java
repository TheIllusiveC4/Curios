package top.theillusivec4.curios.common.triggers;

import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import top.theillusivec4.curios.Curios;

import javax.annotation.Nonnull;

public class EquipCurioTrigger extends AbstractCriterionTrigger<EquipCurioTrigger.Instance> {
	public static final ResourceLocation ID = new ResourceLocation(Curios.MODID, "equip_curio");
	public static final EquipCurioTrigger INSTANCE = new EquipCurioTrigger();

	private EquipCurioTrigger() {
	}

	@Nonnull
	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Nonnull
	@Override
	public EquipCurioTrigger.Instance deserializeTrigger(@Nonnull JsonObject json, @Nonnull EntityPredicate.AndPredicate playerPred, ConditionArrayParser conditions) {
		return new EquipCurioTrigger.Instance(playerPred, ItemPredicate.deserialize(json.get("item")), LocationPredicate.deserialize(json.get("location")));
	}

	public void trigger(ServerPlayerEntity player, ItemStack stack, ServerWorld world, double x, double y, double z) {
		this.triggerListeners(player, instance -> instance.test(stack, world, x, y, z));
	}

	static class Instance extends CriterionInstance {
		private final ItemPredicate item;
		private final LocationPredicate location;

		Instance(EntityPredicate.AndPredicate playerPred, ItemPredicate count, LocationPredicate indexPos) {
			super(ID, playerPred);
			this.item = count;
			this.location = indexPos;
		}

		@Nonnull
		@Override
		public ResourceLocation getId() {
			return ID;
		}

		boolean test(ItemStack stack, ServerWorld world, double x, double y, double z) {
			return this.item.test(stack) && this.location.test(world, x, y, z);
		}
	}
}