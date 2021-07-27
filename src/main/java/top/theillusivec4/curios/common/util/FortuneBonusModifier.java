package top.theillusivec4.curios.common.util;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import top.theillusivec4.curios.api.CuriosApi;

public class FortuneBonusModifier extends LootModifier {

  protected FortuneBonusModifier(LootItemCondition[] conditions) {
    super(conditions);
  }

  @Nonnull
  @Override
  protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
    ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);

    if (tool == null || tool.hasTag() && tool.getTag() != null &&
        tool.getTag().getBoolean("HasCuriosFortuneBonus")) {
      return generatedLoot;
    }
    Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
    BlockState blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);

    if (blockState == null || !(entity instanceof LivingEntity)) {
      return generatedLoot;
    }
    LivingEntity player = (LivingEntity) entity;
    int totalFortuneBonus = CuriosApi.getCuriosHelper().getCuriosHandler(player)
        .map(handler -> handler.getFortuneLevel(context)).orElse(0);

    if (totalFortuneBonus <= 0) {
      return generatedLoot;
    }
    ItemStack fakeTool = tool.isEmpty() ? new ItemStack(Items.BARRIER) : tool.copy();
    fakeTool.getOrCreateTag().putBoolean("HasCuriosFortuneBonus", true);

    Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(fakeTool);
    enchantments.put(Enchantments.BLOCK_FORTUNE,
        EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, fakeTool) + totalFortuneBonus);
    EnchantmentHelper.setEnchantments(enchantments, fakeTool);
    LootContext.Builder builder = new LootContext.Builder(context);
    builder.withParameter(LootContextParams.TOOL, fakeTool);
    LootContext newContext = builder.create(LootContextParamSets.BLOCK);
    LootTable lootTable = context.getLevel().getServer().getLootTables()
        .get(blockState.getBlock().getLootTable());
    return lootTable.getRandomItems(newContext);
  }

  public static class Serializer extends GlobalLootModifierSerializer<FortuneBonusModifier> {

    @Override
    public FortuneBonusModifier read(ResourceLocation location, JsonObject object,
                                     LootItemCondition[] conditions) {
      return new FortuneBonusModifier(conditions);
    }

    @Override
    public JsonObject write(FortuneBonusModifier instance) {
      return this.makeConditions(instance.conditions);
    }
  }
}