package top.theillusivec4.curios.common.capability;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.data.CuriosEntityManager;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;

public class CurioInventory implements INBTSerializable<CompoundTag> {

  final Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
  ICuriosItemHandler curiosItemHandler;
  NonNullList<ItemStack> invalidStacks = NonNullList.create();
  Set<ICurioStacksHandler> updates = new HashSet<>();
  CompoundTag deserialized = new CompoundTag();
  boolean markDeserialized = false;

  public void init(final ICuriosItemHandler curiosItemHandler) {
    this.curiosItemHandler = curiosItemHandler;
    this.curios.clear();
    this.invalidStacks.clear();
    LivingEntity livingEntity = curiosItemHandler.getWearer();
    EntityType<?> type = livingEntity.getType();

    if (!this.markDeserialized) {
      if (!livingEntity.level().isClientSide()) {
        SortedSet<ISlotType> sorted = new TreeSet<>(CuriosApi.getEntitySlots(type).values());

        for (ISlotType slotType : sorted) {
          this.curios.put(slotType.getIdentifier(),
              new CurioStacksHandler(curiosItemHandler, slotType.getIdentifier(),
                  slotType.getSize(), slotType.useNativeGui(), slotType.hasCosmetic(),
                  slotType.canToggleRendering(), slotType.getDropRule()));
        }
      } else {
        Map<String, Integer> slots = CuriosEntityManager.INSTANCE.getClientSlots(type);

        for (Map.Entry<String, Integer> entry : slots.entrySet()) {
          this.curios.put(entry.getKey(),
              new CurioStacksHandler(curiosItemHandler, entry.getKey(), entry.getValue(), true,
                  true, true, ICurio.DropRule.DEFAULT));
        }
      }
    } else {
      ListTag tagList = this.deserialized.getList("Curios", Tag.TAG_COMPOUND);
      Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
      SortedMap<ISlotType, ICurioStacksHandler> sortedCurios = new TreeMap<>();
      SortedSet<ISlotType> sorted =
          new TreeSet<>(CuriosApi.getEntitySlots(livingEntity.getType()).values());

      for (ISlotType slotType : sorted) {
        sortedCurios.put(slotType,
            new CurioStacksHandler(curiosItemHandler, slotType.getIdentifier(),
                slotType.getSize(), slotType.useNativeGui(), slotType.hasCosmetic(),
                slotType.canToggleRendering(), slotType.getDropRule()));
      }

      for (int i = 0; i < tagList.size(); i++) {
        CompoundTag tag = tagList.getCompound(i);
        String identifier = tag.getString("Identifier");
        CurioStacksHandler prevStacksHandler =
            new CurioStacksHandler(curiosItemHandler, identifier);
        prevStacksHandler.deserializeNBT(tag.getCompound("StacksHandler"));

        Optional<ISlotType> optionalType =
            Optional.ofNullable(CuriosApi.getEntitySlots(livingEntity.getType()).get(identifier));
        optionalType.ifPresent(slotType -> {
          CurioStacksHandler newStacksHandler =
              new CurioStacksHandler(curiosItemHandler, slotType.getIdentifier(),
                  slotType.getSize(), slotType.useNativeGui(), slotType.hasCosmetic(),
                  slotType.canToggleRendering(), slotType.getDropRule());
          newStacksHandler.copyModifiers(prevStacksHandler);
          int index = 0;

          while (index < newStacksHandler.getSlots() && index < prevStacksHandler
              .getSlots()) {
            ItemStack prevStack = prevStacksHandler.getStacks().getStackInSlot(index);
            NonNullList<Boolean> renderStates = newStacksHandler.getRenders();
            SlotContext slotContext = new SlotContext(identifier, livingEntity, index, false,
                renderStates.size() > index && renderStates.get(index));

            if (!prevStack.isEmpty()) {

              if (CuriosApi.isStackValid(slotContext, prevStack)) {
                newStacksHandler.getStacks().setStackInSlot(index, prevStack);
              } else {
                this.curiosItemHandler.loseInvalidStack(prevStack);
              }
            }
            ItemStack prevCosmetic = prevStacksHandler.getCosmeticStacks().getStackInSlot(index);
            slotContext = new SlotContext(identifier, livingEntity, index, true, true);

            if (!prevCosmetic.isEmpty()) {

              if (CuriosApi.isStackValid(slotContext, prevCosmetic)) {
                newStacksHandler.getCosmeticStacks().setStackInSlot(index,
                    prevStacksHandler.getCosmeticStacks().getStackInSlot(index));
              } else {
                this.curiosItemHandler.loseInvalidStack(prevCosmetic);
              }
            }
            index++;
          }

          while (index < prevStacksHandler.getSlots()) {
            this.curiosItemHandler.loseInvalidStack(
                prevStacksHandler.getStacks().getStackInSlot(index));
            this.curiosItemHandler.loseInvalidStack(
                prevStacksHandler.getCosmeticStacks().getStackInSlot(index));
            index++;
          }
          sortedCurios.put(slotType, newStacksHandler);

          for (int j = 0;
               j < newStacksHandler.getRenders().size() &&
                   j < prevStacksHandler.getRenders()
                       .size(); j++) {
            newStacksHandler.getRenders().set(j, prevStacksHandler.getRenders().get(j));
          }
        });

        if (optionalType.isEmpty()) {
          IDynamicStackHandler stackHandler = prevStacksHandler.getStacks();
          IDynamicStackHandler cosmeticStackHandler = prevStacksHandler.getCosmeticStacks();

          for (int j = 0; j < stackHandler.getSlots(); j++) {
            ItemStack stack = stackHandler.getStackInSlot(j);

            if (!stack.isEmpty()) {
              this.curiosItemHandler.loseInvalidStack(stack);
            }

            ItemStack cosmeticStack = cosmeticStackHandler.getStackInSlot(j);

            if (!cosmeticStack.isEmpty()) {
              this.curiosItemHandler.loseInvalidStack(cosmeticStack);
            }
          }
        }
      }
      sortedCurios.forEach(
          (slotType, stacksHandler) -> curios.put(slotType.getIdentifier(), stacksHandler));
      this.curios.putAll(curios);
      this.deserialized = new CompoundTag();
      this.markDeserialized = false;
    }
  }

  public Map<String, ICurioStacksHandler> asMap() {
    return this.curios;
  }

  public void replace(Map<String, ICurioStacksHandler> curios) {
    this.curios.clear();
    this.curios.putAll(curios);
  }

  @Override
  public CompoundTag serializeNBT() {
    CompoundTag compound = new CompoundTag();

    ListTag taglist = new ListTag();
    this.curios.forEach((key, stacksHandler) -> {
      CompoundTag tag = new CompoundTag();
      tag.put("StacksHandler", stacksHandler.serializeNBT());
      tag.putString("Identifier", key);
      taglist.add(tag);
    });
    compound.put("Curios", taglist);
    return compound;
  }

  @Override
  public void deserializeNBT(CompoundTag nbt) {
    this.deserialized = nbt;
    this.markDeserialized = true;
  }
}
