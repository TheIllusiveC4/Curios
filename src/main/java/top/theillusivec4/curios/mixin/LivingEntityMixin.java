package top.theillusivec4.curios.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeCallback;
import top.theillusivec4.curios.api.event.CurioDropsCallback;
import top.theillusivec4.curios.api.event.DropRulesCallback;
import top.theillusivec4.curios.api.type.component.ICurio;
import top.theillusivec4.curios.api.type.component.ICurio.DropRule;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

  @Shadow
  int playerHitTimer;

  @Inject(method = "drop", at = @At("TAIL"))
  protected void drop(DamageSource source, CallbackInfo cb) {
    @SuppressWarnings("ConstantConditions") LivingEntity livingEntity = (LivingEntity) (Object) this;
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {
      LivingEntity wearer = handler.getWearer();
      Entity entity = source.getAttacker();
      Collection<ItemStack> drops = new ArrayList<>();
      int looting =
          entity instanceof LivingEntity ? EnchantmentHelper.getLooting((LivingEntity) entity) : 0;
      boolean recentlyHit = playerHitTimer > 0;
      List<Pair<Predicate<ItemStack>, DropRule>> overrides = new ArrayList<>();
      DropRulesCallback.EVENT.invoker()
          .dropRules(wearer, handler, source, looting, recentlyHit, overrides);
      boolean keepInventory = wearer.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY);

      handler.getCurios().forEach((id, stacksHandler) -> {
        handleDrops(wearer, overrides, stacksHandler.getStacks(), drops, keepInventory);
        handleDrops(wearer, overrides, stacksHandler.getCosmeticStacks(), drops, keepInventory);
      });

      if (CurioDropsCallback.EVENT.invoker()
          .drop(livingEntity, handler, source, drops, looting, recentlyHit)) {

        if (wearer instanceof PlayerEntity) {
          drops.forEach(stack -> ((PlayerEntity) wearer).dropItem(stack, true, false));
        } else {
          drops.forEach(livingEntity::dropStack);
        }
      }
    });
  }

  @Inject(method = "tick", at = @At("TAIL"))
  public void tick(CallbackInfo cb) {
    @SuppressWarnings("ConstantConditions") LivingEntity livingEntity = (LivingEntity) (Object) this;
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {
      Map<String, ICurioStacksHandler> curios = handler.getCurios();

      for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
        ICurioStacksHandler stacksHandler = entry.getValue();
        String identifier = entry.getKey();
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();
        IDynamicStackHandler cosmeticStackHandler = stacksHandler.getCosmeticStacks();

        for (int i = 0; i < stackHandler.size(); i++) {
          ItemStack stack = stackHandler.getStack(i);
          Optional<ICurio> currentCurio = CuriosApi.getCuriosHelper().getCurio(stack);
          final int index = i;

          if (!stack.isEmpty()) {
            stack.inventoryTick(livingEntity.world, livingEntity, -1, false);
            currentCurio.ifPresent(curio -> {
              curio.curioTick(identifier, index, livingEntity);

              if (livingEntity.world.isClient()) {
                curio.curioAnimate(identifier, index, livingEntity);
              }
            });
          }

          if (!livingEntity.world.isClient()) {
            ItemStack prevStack = stackHandler.getPreviousStack(i);
            boolean sync = false;

            if (!ItemStack.areEqual(stack, prevStack)) {
              Optional<ICurio> prevCurio = CuriosApi.getCuriosHelper().getCurio(prevStack);
              sync = true;
              CurioChangeCallback.EVENT.invoker()
                  .change(livingEntity, identifier, i, prevStack, stack);
              livingEntity.getAttributes().removeModifiers(
                  CuriosApi.getCuriosHelper().getAttributeModifiers(identifier, prevStack));
              livingEntity.getAttributes().addTemporaryModifiers(
                  CuriosApi.getCuriosHelper().getAttributeModifiers(identifier, stack));
              prevCurio.ifPresent(curio -> curio.onUnequip(identifier, index, livingEntity));
              currentCurio.ifPresent(curio -> curio.onEquip(identifier, index, livingEntity));
              stackHandler.setPreviousStack(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
            ItemStack cosmeticStack = cosmeticStackHandler.getStack(i);
            ItemStack prevCosmeticStack = cosmeticStackHandler.getPreviousStack(i);

            if (!ItemStack.areEqual(cosmeticStack, prevCosmeticStack)) {
              sync = true;
              cosmeticStackHandler.setPreviousStack(index,
                  cosmeticStack.isEmpty() ? ItemStack.EMPTY : cosmeticStack.copy());
            }

            if (sync) {
              handler.sync();
            }
          }
        }
      }
    });
  }

  private static void handleDrops(LivingEntity livingEntity,
      List<Pair<Predicate<ItemStack>, DropRule>> dropRules, IDynamicStackHandler stacks,
      Collection<ItemStack> drops, boolean keepInventory) {

    for (int i = 0; i < stacks.size(); i++) {
      ItemStack stack = stacks.getStack(i);

      if (!stack.isEmpty()) {
        DropRule dropRuleOverride = null;

        for (Pair<Predicate<ItemStack>, DropRule> override : dropRules) {

          if (override.getLeft().test(stack)) {
            dropRuleOverride = override.getRight();
          }
        }
        DropRule dropRule = dropRuleOverride != null ? dropRuleOverride
            : CuriosApi.getCuriosHelper().getCurio(stack)
                .map(curio -> curio.getDropRule(livingEntity)).orElse(DropRule.DEFAULT);

        if ((dropRule == DropRule.DEFAULT && keepInventory) || dropRule == DropRule.ALWAYS_KEEP) {
          continue;
        }

        if (!EnchantmentHelper.hasVanishingCurse(stack) && dropRule != DropRule.DESTROY) {
          drops.add(stack);
        }
        stacks.setStack(i, ItemStack.EMPTY);
      }
    }
  }
}
