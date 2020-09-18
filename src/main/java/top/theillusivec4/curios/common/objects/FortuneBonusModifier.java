package top.theillusivec4.curios.common.objects;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class FortuneBonusModifier extends LootModifier {

	protected FortuneBonusModifier(ILootCondition[] conditions) {
		super(conditions);
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
		ItemStack tool = context.get(LootParameters.TOOL);

		if (tool == null || tool.getOrCreateTag().getBoolean("HasCuriosFortuneBonus"))
			return generatedLoot;

		Entity entity = context.get(LootParameters.THIS_ENTITY);
		BlockState blockState = context.get(LootParameters.BLOCK_STATE);
		if (blockState == null || !(entity instanceof LivingEntity))
			return generatedLoot;

		LivingEntity player = (LivingEntity) entity;
		int totalFortuneBonus = 0;

		if (CuriosApi.getCuriosHelper().getCuriosHandler(player).isPresent()) {
			ICuriosItemHandler handler = CuriosApi.getCuriosHelper().getCuriosHandler(player).orElse(null);
			Map<String, ICurioStacksHandler> curios = handler.getCurios();
			
			for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
				ICurioStacksHandler stacksHandler = entry.getValue();
				String identifier = entry.getKey();
				IDynamicStackHandler stackHandler = stacksHandler.getStacks();

				for (int i = 0; i < stackHandler.getSlots(); i++) {
					ItemStack stack = stackHandler.getStackInSlot(i);
					LazyOptional<ICurio> curioCapability = CuriosApi.getCuriosHelper().getCurio(stack);
					final int index = i;

					if (!player.world.isRemote && !stack.isEmpty()) {
						if (curioCapability.isPresent()) {
							totalFortuneBonus += curioCapability.orElseGet(null).getFortuneBonus(identifier, player, stack, index);
						}
					}
				}
			}
		}

		if (totalFortuneBonus <= 0)
			return generatedLoot;

		ItemStack fakeTool = tool.isEmpty() ? new ItemStack(Items.BARRIER) : tool.copy();
		fakeTool.getOrCreateTag().putBoolean("HasCuriosFortuneBonus", true);

		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(fakeTool);
		enchantments.put(Enchantments.FORTUNE, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, fakeTool) + totalFortuneBonus);
		EnchantmentHelper.setEnchantments(enchantments, fakeTool);
		LootContext.Builder builder = new LootContext.Builder(context);
		builder.withParameter(LootParameters.TOOL, fakeTool);
		LootContext newContext = builder.build(LootParameterSets.BLOCK);
		LootTable lootTable = context.getWorld().getServer().getLootTableManager().getLootTableFromLocation(blockState.getBlock().getLootTable());
		return lootTable.generate(newContext);
	}

	public static class Serializer extends GlobalLootModifierSerializer<FortuneBonusModifier> {

		@Override
		public FortuneBonusModifier read(ResourceLocation location, JsonObject object, ILootCondition[] conditions) {
			return new FortuneBonusModifier(conditions);
		}

		@Override
		public JsonObject write(FortuneBonusModifier instance) {
			return this.makeConditions(instance.conditions);
		}
	}
}
