package top.theillusivec4.curios.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.entity.LivingEntity;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.util.ICuriosServer;

public class CuriosServer implements ICuriosServer {

  private Map<String, ISlotType> idToType = new HashMap<>();

  @Override
  public void addSlotType(ISlotType slotType) {
    this.idToType.put(slotType.getIdentifier(), slotType);
  }

  @Override
  public Optional<ISlotType> getSlotType(String identifier) {
    return Optional.ofNullable(this.idToType.get(identifier));
  }

  @Override
  public Collection<ISlotType> getSlotTypes() {
    return Collections.unmodifiableCollection(idToType.values());
  }

  @Override
  public Set<String> getSlotTypeIds() {
    return Collections.unmodifiableSet(idToType.keySet());
  }

  @Override
  public int getSlotsForType(@Nonnull final LivingEntity livingEntity, String identifier) {
    return CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).map(
        handler -> handler.getStacksHandler(identifier).map(ICurioStacksHandler::getSlots)
            .orElse(0)).orElse(0);
  }

  @Override
  public void setSlotsForType(String id, final LivingEntity livingEntity, int amount) {
    int difference = amount - getSlotsForType(livingEntity, id);

    if (difference > 0) {
      growSlotType(id, difference, livingEntity);
    } else if (difference < 0) {
      shrinkSlotType(id, Math.abs(difference), livingEntity);
    }
  }

  @Override
  public void growSlotType(String id, final LivingEntity livingEntity) {
    growSlotType(id, 1, livingEntity);
  }

  @Override
  public void growSlotType(String id, int amount, final LivingEntity livingEntity) {
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity)
        .ifPresent(handler -> handler.growSlotType(id, amount));
  }


  @Override
  public void shrinkSlotType(String id, final LivingEntity livingEntity) {
    shrinkSlotType(id, 1, livingEntity);
  }

  @Override
  public void shrinkSlotType(String id, int amount, final LivingEntity livingEntity) {
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity)
        .ifPresent(handler -> handler.shrinkSlotType(id, amount));
  }


  @Override
  public void unlockSlotType(String id, final LivingEntity livingEntity) {
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity)
        .ifPresent(handler -> handler.unlockSlotType(id));
  }

  @Override
  public void lockSlotType(String id, final LivingEntity livingEntity) {
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity)
        .ifPresent(handler -> handler.lockSlotType(id));
  }
}
