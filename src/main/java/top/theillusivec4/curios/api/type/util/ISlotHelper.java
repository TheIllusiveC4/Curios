package top.theillusivec4.curios.api.type.util;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import net.minecraft.entity.LivingEntity;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.slottype.SlotType;

public interface ISlotHelper {

  void addSlotType(ISlotType slotType);

  Optional<ISlotType> getSlotType(String identifier);

  Collection<ISlotType> getSlotTypes();

  SortedMap<ISlotType, ICurioStacksHandler> createSlots();

  Set<String> getSlotTypeIds();

  /**
   * Retrieves the number of slots that an entity has for a specific curio type.
   *
   * @param livingEntity The entity that has the slot
   * @param identifier   The type identifier of the slot
   * @return The number of slots
   */
  int getSlotsForType(LivingEntity livingEntity, String identifier);

  /**
   * Sets the number of slots that an entity has for a specific curio type.
   *
   * @param livingEntity The entity that has the slot
   * @param id           The type identifier of the slot
   * @param amount       The number of slots
   */
  void setSlotsForType(String id, LivingEntity livingEntity, int amount);

  /**
   * /** Adds a single slot to the {@link SlotType} with the associated identifier. If the slot to
   * be added is for a type that is not enabled on the entity, it will not be added. For adding
   * slot(s) for types that are not yet available, there must first be a call to {@link
   * ISlotHelper#unlockSlotType(String, LivingEntity)}
   *
   * @param id           The identifier of the CurioType
   * @param livingEntity The holder of the slot(s)
   */
  void growSlotType(String id, LivingEntity livingEntity);

  /**
   * Adds multiple slots to the {@link SlotType} with the associated identifier. If the slot to be
   * added is for a type that is not enabled on the entity, it will not be added. For adding slot(s)
   * for types that are not yet available, there must first be a call to {@link
   * ISlotHelper#unlockSlotType(String, LivingEntity)}
   *
   * @param id           The identifier of the CurioType
   * @param amount       The number of slots to add
   * @param livingEntity The holder of the slots
   */
  void growSlotType(String id, int amount, LivingEntity livingEntity);

  /**
   * Removes a single slot to the {@link SlotType} with the associated identifier. If the slot to be
   * removed is the last slot available, it will not be removed. For the removal of the last slot,
   * please see {@link ISlotHelper#lockSlotType(String, LivingEntity)}
   *
   * @param id           The identifier of the CurioType
   * @param livingEntity The holder of the slot(s)
   */
  void shrinkSlotType(String id, LivingEntity livingEntity);

  /**
   * Removes multiple slots to the {@link SlotType} with the associated identifier. If the slot to
   * be removed is the last slot available, it will not be removed. For the removal of the last
   * slot, please see {@link ISlotHelper#lockSlotType(String, LivingEntity)}
   *
   * @param id           The identifier of the CurioType
   * @param livingEntity The holder of the slot(s)
   */
  void shrinkSlotType(String id, int amount, LivingEntity livingEntity);

  /**
   * Adds a {@link SlotType} to the entity The number of slots given is the type's default.
   *
   * @param id           The identifier of the CurioType
   * @param livingEntity The holder of the slot(s)
   */
  void unlockSlotType(String id, LivingEntity livingEntity);

  /**
   * Removes a {@link SlotType} from the entity.
   *
   * @param id           The identifier of the CurioType
   * @param livingEntity The holder of the slot(s)
   */
  void lockSlotType(String id, final LivingEntity livingEntity);
}
