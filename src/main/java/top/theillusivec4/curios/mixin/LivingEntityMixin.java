package top.theillusivec4.curios.mixin;

import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeCallback;
import top.theillusivec4.curios.api.type.component.ICurio;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

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
                  .onChange(livingEntity, identifier, i, prevStack, stack);
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
}
