package top.theillusivec4.curios.mixin;

import java.util.Map;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CuriosMixinHooks {

  private static final ITagManager<Item> ITEM_TAGS = ForgeRegistries.ITEMS.tags();

  public static boolean hasEnderMask(LivingEntity livingEntity, EnderMan enderMan) {
    return CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).map(handler -> {

      for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
        IDynamicStackHandler stacks = entry.getValue().getStacks();

        for (int i = 0; i < stacks.getSlots(); i++) {
          final int index = i;
          NonNullList<Boolean> renderStates = entry.getValue().getRenders();
          boolean hasMask =
              CuriosApi.getCuriosHelper().getCurio(stacks.getStackInSlot(i)).map(curio -> curio
                      .isEnderMask(new SlotContext(entry.getKey(), livingEntity, index, false,
                          renderStates.size() > index && renderStates.get(index)), enderMan))
                  .orElse(false);

          if (hasMask) {
            return true;
          }
        }
      }
      return false;
    }).orElse(false);
  }

  public static boolean canNeutralizePiglins(LivingEntity livingEntity) {
    return CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).map(handler -> {

      for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
        IDynamicStackHandler stacks = entry.getValue().getStacks();

        for (int i = 0; i < stacks.getSlots(); i++) {
          final int index = i;
          NonNullList<Boolean> renderStates = entry.getValue().getRenders();
          boolean canNeutralize =
              CuriosApi.getCuriosHelper().getCurio(stacks.getStackInSlot(i)).map(curio -> curio
                      .makesPiglinsNeutral(new SlotContext(entry.getKey(), livingEntity, index, false,
                          renderStates.size() > index && renderStates.get(index))))
                  .orElse(false);

          if (canNeutralize) {
            return true;
          }
        }
      }
      return false;
    }).orElse(false);
  }

  public static boolean canWalkOnPowderSnow(LivingEntity livingEntity) {
    return CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).map(handler -> {

      for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
        IDynamicStackHandler stacks = entry.getValue().getStacks();

        for (int i = 0; i < stacks.getSlots(); i++) {
          final int index = i;
          NonNullList<Boolean> renderStates = entry.getValue().getRenders();
          boolean canWalk =
              CuriosApi.getCuriosHelper().getCurio(stacks.getStackInSlot(i)).map(curio -> curio
                      .canWalkOnPowderedSnow(new SlotContext(entry.getKey(), livingEntity, index, false,
                          renderStates.size() > index && renderStates.get(index))))
                  .orElse(false);

          if (canWalk) {
            return true;
          }
        }
      }
      return false;
    }).orElse(false);
  }

  public static int getFortuneLevel(Player player) {
    return CuriosApi.getCuriosHelper().getCuriosHandler(player)
        .map(handler -> handler.getFortuneLevel(null)).orElse(0);
  }

  public static int getFortuneLevel(LootContext lootContext) {
    Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);

    if (entity instanceof LivingEntity livingEntity) {
      return CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity)
          .map(handler -> handler.getFortuneLevel(lootContext)).orElse(0);
    } else {
      return 0;
    }
  }

  public static boolean isFreezeImmune(LivingEntity livingEntity) {
    return CuriosApi.getCuriosHelper().getEquippedCurios(livingEntity).map(curios -> {

      for (int i = 0; i < curios.getSlots(); i++) {
        ItemStack stack = curios.getStackInSlot(i);

        if (ITEM_TAGS != null &&
            ITEM_TAGS.getTag(ItemTags.FREEZE_IMMUNE_WEARABLES).contains(stack.getItem())) {
          return true;
        }
      }
      return false;
    }).orElse(false);
  }
}
