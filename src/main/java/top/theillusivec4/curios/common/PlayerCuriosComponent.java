package top.theillusivec4.curios.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.component.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;

public class PlayerCuriosComponent implements ICuriosItemHandler {

  Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
  Set<String> locked = new HashSet<>();
  DefaultedList<ItemStack> invalidStacks = DefaultedList.of();
  PlayerEntity wearer;

  public PlayerCuriosComponent(PlayerEntity playerEntity) {
    this.wearer = playerEntity;
    this.reset();
  }

  @Override
  public Map<String, ICurioStacksHandler> getCurios() {
    return Collections.unmodifiableMap(this.curios);
  }

  @Override
  public void setCurios(Map<String, ICurioStacksHandler> map) {
    this.curios = map;
  }

  @Override
  public int getSlots() {
    return this.curios.values().stream().mapToInt(ICurioStacksHandler::getSlots).sum();
  }

  @Override
  public Set<String> getLockedSlots() {
    return Collections.unmodifiableSet(this.locked);
  }

  @Override
  public void reset() {

    if (!this.wearer.getEntityWorld().isClient() && wearer instanceof ServerPlayerEntity) {
      this.locked.clear();
      this.curios.clear();
      this.invalidStacks.clear();
      CuriosApi.getSlotHelper().createSlots().forEach(
          ((slotType, stacksHandler) -> curios.put(slotType.getIdentifier(), stacksHandler)));
    }
  }

  @Override
  public Optional<ICurioStacksHandler> getStacksHandler(String identifier) {
    return Optional.ofNullable(this.curios.get(identifier));
  }

  @Override
  public void unlockSlotType(String identifier, int amount, boolean visible, boolean cosmetic) {
    this.curios.putIfAbsent(identifier, new CurioStacksHandler(amount, 0, visible, cosmetic));
    this.locked.remove(identifier);
  }

  @Override
  public void lockSlotType(String identifier) {
    this.getStacksHandler(identifier).ifPresent(stackHandler -> {
      this.curios.remove(identifier);
      this.locked.add(identifier);
      this.loseStacks(stackHandler.getStacks(), identifier, stackHandler.getSlots());
    });
  }

  @Override
  public void growSlotType(String identifier, int amount) {

    if (amount > 0) {
      this.getStacksHandler(identifier).ifPresent(stackHandler -> stackHandler.grow(amount));
    }
  }

  @Override
  public void shrinkSlotType(String identifier, int amount) {
    if (amount > 0) {
      this.getStacksHandler(identifier).ifPresent(stackHandler -> {
        int toShrink = Math.min(stackHandler.getSlots() - 1, amount);
        this.loseStacks(stackHandler.getStacks(), identifier, toShrink);
        stackHandler.shrink(amount);
      });
    }
  }

  @Override
  public LivingEntity getWearer() {
    return this.wearer;
  }

  @Override
  public void loseInvalidStack(ItemStack stack) {
    this.invalidStacks.add(stack);
  }

  @Override
  public void handleInvalidStacks() {

    if (this.wearer != null && !this.invalidStacks.isEmpty()) {
      this.invalidStacks.forEach(drop -> giveItemToPlayer(wearer, drop));
      this.invalidStacks = DefaultedList.of();
    }
  }

  @Override
  public Entity getEntity() {
    return this.wearer;
  }

  @Override
  public void fromTag(CompoundTag compoundTag) {
    ListTag tagList = compoundTag.getList("Curios", 10);
    ListTag lockedList = compoundTag.getList("Locked", 8);

    if (!tagList.isEmpty()) {
      Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
      SortedMap<ISlotType, ICurioStacksHandler> sortedCurios = CuriosApi.getSlotHelper()
          .createSlots();

      for (int i = 0; i < tagList.size(); i++) {
        CompoundTag tag = tagList.getCompound(i);
        String identifier = tag.getString("Identifier");
        CurioStacksHandler prevStacksHandler = new CurioStacksHandler();
        prevStacksHandler.deserializeTag(tag.getCompound("StacksHandler"));

        Optional<ISlotType> optionalType = CuriosApi.getSlotHelper().getSlotType(identifier);
        optionalType.ifPresent(type -> {
          CurioStacksHandler newStacksHandler = new CurioStacksHandler(type.getSize(),
              prevStacksHandler.getSizeShift(), type.isVisible(), type.hasCosmetic());
          int index = 0;

          while (index < newStacksHandler.getSlots() && index < prevStacksHandler.getSlots()) {
            newStacksHandler.getStacks()
                .setStack(index, prevStacksHandler.getStacks().getStack(index));
            newStacksHandler.getCosmeticStacks()
                .setStack(index, prevStacksHandler.getCosmeticStacks().getStack(index));
            index++;
          }

          while (index < prevStacksHandler.getSlots()) {
            this.loseInvalidStack(prevStacksHandler.getStacks().getStack(index));
            this.loseInvalidStack(prevStacksHandler.getCosmeticStacks().getStack(index));
            index++;
          }
          sortedCurios.put(type, newStacksHandler);

          for (int j = 0;
              j < newStacksHandler.getRenders().size() && j < prevStacksHandler.getRenders().size();
              j++) {
            newStacksHandler.getRenders().set(j, prevStacksHandler.getRenders().get(j));
          }
        });

        if (!optionalType.isPresent()) {
          IDynamicStackHandler stackHandler = prevStacksHandler.getStacks();
          IDynamicStackHandler cosmeticStackHandler = prevStacksHandler.getCosmeticStacks();

          for (int j = 0; j < stackHandler.size(); j++) {
            ItemStack stack = stackHandler.getStack(j);

            if (!stack.isEmpty()) {
              this.loseInvalidStack(stack);
            }

            ItemStack cosmeticStack = cosmeticStackHandler.getStack(j);

            if (!cosmeticStack.isEmpty()) {
              this.loseInvalidStack(cosmeticStack);
            }
          }
        }
      }
      sortedCurios.forEach(
          (slotType, stacksHandler) -> curios.put(slotType.getIdentifier(), stacksHandler));
      this.setCurios(curios);

      for (int k = 0; k < lockedList.size(); k++) {
        this.lockSlotType(lockedList.getString(k));
      }
    }
  }

  @Override
  public CompoundTag toTag(CompoundTag compoundTag) {

    ListTag taglist = new ListTag();
    this.getCurios().forEach((key, stacksHandler) -> {
      CompoundTag tag = new CompoundTag();
      tag.put("StacksHandler", stacksHandler.serializeTag());
      tag.putString("Identifier", key);
      taglist.add(tag);
    });
    compoundTag.put("Curios", taglist);

    ListTag taglist1 = new ListTag();

    for (String identifier : this.getLockedSlots()) {
      taglist1.add(StringTag.of(identifier));
    }
    compoundTag.put("Locked", taglist1);
    return compoundTag;
  }

  private void loseStacks(IDynamicStackHandler stackHandler, String identifier, int amount) {

    if (this.wearer != null && !this.wearer.getEntityWorld().isClient()) {
      List<ItemStack> drops = new ArrayList<>();

      for (int i = stackHandler.size() - amount; i < stackHandler.size(); i++) {
        ItemStack stack = stackHandler.getStack(i);
        drops.add(stackHandler.getStack(i));

        if (!stack.isEmpty()) {
          wearer.getAttributes().removeModifiers(
              CuriosApi.getCuriosHelper().getAttributeModifiers(identifier, stack));
          int index = i;
          CuriosApi.getCuriosHelper().getCurio(stack)
              .ifPresent(curio -> curio.onUnequip(identifier, index, this.wearer));
        }
        stackHandler.setStack(i, ItemStack.EMPTY);
      }
      drops.forEach(drop -> giveItemToPlayer(wearer, drop));
    }
  }

  private static void giveItemToPlayer(PlayerEntity playerEntity, ItemStack stack) {
    boolean bl = playerEntity.inventory.insertStack(stack);
    ItemEntity itemEntity;

    if (bl && stack.isEmpty()) {
      stack.setCount(1);
      itemEntity = playerEntity.dropItem(stack, false);

      if (itemEntity != null) {
        itemEntity.setDespawnImmediately();
      }
      playerEntity.world
          .playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(),
              SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
              ((playerEntity.getRandom().nextFloat() - playerEntity.getRandom().nextFloat()) * 0.7F
                  + 1.0F) * 2.0F);
      playerEntity.playerScreenHandler.sendContentUpdates();
    } else {
      itemEntity = playerEntity.dropItem(stack, false);

      if (itemEntity != null) {
        itemEntity.resetPickupDelay();
        itemEntity.setOwner(playerEntity.getUuid());
      }
    }
  }
}
