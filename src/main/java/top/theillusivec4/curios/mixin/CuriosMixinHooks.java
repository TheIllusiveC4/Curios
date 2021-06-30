package top.theillusivec4.curios.mixin;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CuriosMixinHooks {

  public static boolean canNeutralizePiglins(LivingEntity livingEntity) {
    return CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).map(handler -> {

      for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
        IDynamicStackHandler stacks = entry.getValue().getStacks();

        for (int i = 0; i < stacks.getSlots(); i++) {
          final int index = i;
          boolean canNeutralize =
              CuriosApi.getCuriosHelper().getCurio(stacks.getStackInSlot(i)).map(curio -> curio
                  .makesPiglinsNeutral(new SlotContext(entry.getKey(), livingEntity, index)))
                  .orElse(false);

          if (canNeutralize) {
            return true;
          }
        }
      }
      return false;
    }).orElse(false);
  }

  public static int getFortuneLevel(Entity entity, LootContext lootContext) {

    if (entity instanceof LivingEntity) {
      LivingEntity livingEntity = (LivingEntity) entity;
      AtomicInteger fortuneLevel = new AtomicInteger();
      CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {

        for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
          IDynamicStackHandler stacks = entry.getValue().getStacks();

          for (int i = 0; i < stacks.getSlots(); i++) {
            final int index = i;
            fortuneLevel
                .addAndGet(CuriosApi.getCuriosHelper().getCurio(stacks.getStackInSlot(i)).map(
                    curio -> curio
                        .getFortuneLevel(new SlotContext(entry.getKey(), livingEntity, index),
                            lootContext)).orElse(0));
          }
        }
      });
      return fortuneLevel.get();
    } else {
      return 0;
    }
  }
}
