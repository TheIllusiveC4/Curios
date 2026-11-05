package top.theillusivec4.curios.common.inventory;

import java.util.Objects;
import net.minecraft.core.NonNullList;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;
import top.theillusivec4.curios.api.event.CurioCanUnequipEvent;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@NullMarked
public class CuriosStacksResourceHandler extends ItemStacksResourceHandler implements
    IDynamicStackHandler {

  protected final ISlotType slotType;
  @Nullable
  protected final LivingEntity livingEntity;
  protected NonNullList<Boolean> renderable;
  protected NonNullList<Boolean> active;
  protected NonNullList<ItemStack> previousStacks;
  protected NonNullList<Boolean> previousActive;
  protected final boolean isCosmetic;

  public CuriosStacksResourceHandler(ISlotType slotType, @Nullable LivingEntity livingEntity,
                                     boolean isCosmetic) {
    super(slotType.getSize());
    this.slotType = slotType;
    this.livingEntity = livingEntity;
    int size = slotType.getSize();
    this.renderable = NonNullList.withSize(size, true);
    this.active = NonNullList.withSize(size, true);
    this.previousActive = NonNullList.withSize(size, true);
    this.previousStacks = NonNullList.withSize(size, ItemStack.EMPTY);
    this.isCosmetic = isCosmetic;
  }

  public CuriosStacksResourceHandler(ISlotType slotType, @Nullable LivingEntity livingEntity,
                                     boolean isCosmetic, NonNullList<ItemStack> stacks) {
    super(stacks);
    this.slotType = slotType;
    this.livingEntity = livingEntity;
    int size = slotType.getSize();
    this.renderable = NonNullList.withSize(size, true);
    this.active = NonNullList.withSize(size, true);
    this.previousActive = NonNullList.withSize(size, true);
    this.previousStacks = NonNullList.withSize(size, ItemStack.EMPTY);
    this.isCosmetic = isCosmetic;
  }

  public void setSize(int size) {
    TransferPreconditions.checkNonNegative(size);
    int currentSize = this.size();

    if (currentSize != size) {
      NonNullList<ItemStack> resizedList = NonNullList.withSize(size, this.emptyStack);
      NonNullList<ItemStack> resizedPrevious = NonNullList.withSize(size, this.emptyStack);
      NonNullList<Boolean> resizedRenders = NonNullList.withSize(size, true);
      NonNullList<Boolean> resizedActives = NonNullList.withSize(size, true);
      NonNullList<Boolean> resizedPrevActives = NonNullList.withSize(size, true);

      for (int i = 0; i < size && i < currentSize; i++) {
        resizedList.set(i, ItemUtil.getStack(this, i));
        resizedPrevious.set(i, this.previousStacks.get(i));
        resizedRenders.set(i, this.renderable.get(i));
        resizedActives.set(i, this.active.get(i));
        resizedPrevActives.set(i, this.previousActive.get(i));
      }
      this.setStacks(resizedList);
      this.previousStacks = resizedPrevious;
      this.renderable = resizedRenders;
      this.active = resizedActives;
      this.previousActive = resizedPrevActives;
    }
  }

  public SlotContext getSlotContext(int index) {
    return new SlotContext(this.slotType.getId(), this.livingEntity, index, this.isCosmetic,
        this.isRendering(index));
  }

  public boolean isRendering(int index) {
    Objects.checkIndex(index, this.size());
    return this.renderable.get(index);
  }

  public NonNullList<Boolean> getRenders() {
    return this.renderable;
  }

  public boolean isActive(int index) {
    Objects.checkIndex(index, this.size());
    return this.active.get(index);
  }

  public NonNullList<Boolean> getActiveStates() {
    return this.active;
  }

  public NonNullList<Boolean> getPreviousActive() {
    return this.previousActive;
  }

  @Override
  public boolean isValid(int index, ItemResource resource) {
    ItemStack stack = resource.toStack();
    SlotContext ctx = this.getSlotContext(index);
    boolean originalResult = (CuriosApi.isStackValid(ctx, stack)
        && CuriosApi.getCurio(stack).map(curio -> curio.canEquip(ctx)).orElse(true));
    CurioCanEquipEvent equipEvent = new CurioCanEquipEvent(stack, ctx, originalResult);
    NeoForge.EVENT_BUS.post(equipEvent);
    TriState result = equipEvent.getEquipResult();
    return (result == TriState.DEFAULT && originalResult) || result == TriState.TRUE;
  }

  @Override
  public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
    ItemStack existing = ItemUtil.getStack(this, index);
    SlotContext ctx = this.getSlotContext(index);
    boolean isCreative = ctx.entity() instanceof Player player && player.isCreative();
    boolean originalResult = (existing.isEmpty()
        || isCreative
        || !EnchantmentHelper.has(existing, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE))
        && CuriosApi.getCurio(existing).map(curio -> curio.canUnequip(ctx)).orElse(true);
    CurioCanUnequipEvent unequipEvent = new CurioCanUnequipEvent(existing, ctx, originalResult);
    NeoForge.EVENT_BUS.post(unequipEvent);
    TriState result = unequipEvent.getUnequipResult();

    if ((result == TriState.DEFAULT && originalResult) || result == TriState.TRUE) {
      return super.extract(index, resource, amount, transaction);
    }
    return 0;
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    int count = stack.count();
    TransferPreconditions.checkNonNegative(count);

    if (stack.isEmpty() && count > 0) {
      throw new IllegalArgumentException("Stack is empty but the count is positive: " + count);
    }
    ItemStack oldContents = this.stacks.set(slot, stack);
    this.onContentsChanged(slot, oldContents);
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    Objects.checkIndex(slot, this.size());
    return this.stacks.get(slot);
  }

  @Override
  public void setPreviousStackInSlot(int slot, ItemStack stack) {
    Objects.checkIndex(slot, this.size());
    this.previousStacks.set(slot, stack);
  }

  @Override
  public ItemStack getPreviousStackInSlot(int slot) {
    Objects.checkIndex(slot, this.size());
    return this.previousStacks.get(slot);
  }

  @Override
  public int getSlots() {
    return this.size();
  }

  @Override
  public void grow(int amount) {
    int currentSize = this.size();
    this.setSize(currentSize + amount);
  }

  @Override
  public void shrink(int amount) {
    int currentSize = this.size();
    this.setSize(Math.max(0, currentSize - amount));
  }
}
