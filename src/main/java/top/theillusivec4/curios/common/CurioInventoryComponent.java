package top.theillusivec4.curios.common;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import top.theillusivec4.curios.api.type.component.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public class CurioInventoryComponent implements ICuriosItemHandler {

  LivingEntity wearer;

  public CurioInventoryComponent(LivingEntity livingEntity) {
    this.wearer = livingEntity;
  }

  @Override
  public Map<String, ICurioStacksHandler> getCurios() {
    return null;
  }

  @Override
  public void setCurios(Map<String, ICurioStacksHandler> map) {

  }

  @Override
  public int getSlots() {
    return 0;
  }

  @Override
  public Set<String> getLockedSlots() {
    return null;
  }

  @Override
  public void reset() {

  }

  @Override
  public Optional<ICurioStacksHandler> getStacksHandler(String identifier) {
    return Optional.empty();
  }

  @Override
  public void unlockSlotType(String identifier, int amount, boolean visible, boolean cosmetic) {

  }

  @Override
  public void lockSlotType(String identifier) {

  }

  @Override
  public void growSlotType(String identifier, int amount) {

  }

  @Override
  public void shrinkSlotType(String identifier, int amount) {

  }

  @Override
  public LivingEntity getWearer() {
    return this.wearer;
  }

  @Override
  public void loseInvalidStack(ItemStack stack) {

  }

  @Override
  public void handleInvalidStacks() {

  }

  @Override
  public Entity getEntity() {
    return this.wearer;
  }

  @Override
  public void fromTag(CompoundTag compoundTag) {

  }

  @Override
  public CompoundTag toTag(CompoundTag compoundTag) {
    return new CompoundTag();
  }
}
