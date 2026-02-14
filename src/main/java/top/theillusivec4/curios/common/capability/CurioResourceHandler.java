package top.theillusivec4.curios.common.capability;

import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.impl.CuriosRegistry;

public class CurioResourceHandler extends CombinedResourceHandler<ItemResource> {

  public static CurioResourceHandler from(final LivingEntity livingEntity) {
    CurioInventory inv = livingEntity.getData(CuriosRegistry.INVENTORY.get());
    Map<String, ICurioStacksHandler> curios = inv.curios;
    CurioStacksWrapper[] wrappers = new CurioStacksWrapper[curios.size()];
    int index = 0;

    for (ICurioStacksHandler stacksHandler : curios.values()) {

      if (index < wrappers.length) {
        wrappers[index] = new CurioStacksWrapper(stacksHandler.getStacks());
        index++;
      }
    }
    return new CurioResourceHandler(wrappers);
  }

  @SafeVarargs
  public CurioResourceHandler(ResourceHandler<ItemResource>... handlers) {
    super(handlers);
  }

  public static class CurioStacksWrapper extends ItemStacksResourceHandler {

    final IDynamicStackHandler curioStacks;

    public CurioStacksWrapper(IDynamicStackHandler stackHandler) {
      super(stackHandler.getSlots());

      for (int i = 0; i < stackHandler.getSlots(); i++) {
        this.stacks.set(i, stackHandler.getStackInSlot(i));
      }
      this.curioStacks = stackHandler;
    }

    @Override
    protected int getCapacity(int index, @Nonnull ItemResource resource) {
      Objects.checkIndex(index, this.size());
      return this.curioStacks.getSlotLimit(index);
    }

    @Override
    public boolean isValid(int index, @Nonnull ItemResource resource) {
      Objects.checkIndex(index, this.size());
      return this.curioStacks.isItemValid(index, resource.toStack());
    }

    @Override
    protected void onContentsChanged(int index, @Nonnull ItemStack previousContents) {
      Objects.checkIndex(index, this.size());
      this.curioStacks.setStackInSlot(index, this.stacks.get(index));
      this.curioStacks.setPreviousStackInSlot(index, previousContents.copy());
    }
  }
}
