/*
 * Copyright (c) 2018-2024 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package top.theillusivec4.curios.common.capability;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.datafixers.util.Pair;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import top.theillusivec4.curios.api.CuriosResources;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;

public class CurioInventory implements ValueIOSerializable {

  private final LivingEntity owner;

  Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
  NonNullList<ItemStack> invalidStacks = NonNullList.create();
  Set<ICurioStacksHandler> updates = new HashSet<>();

  final Cache<String, Pair<Long, Optional<SlotResult>>> firstCurioCache =
      CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(1, TimeUnit.SECONDS).build();
  final Cache<String, Pair<Long, List<SlotResult>>> findCuriosCache =
      CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(1, TimeUnit.SECONDS).build();

  public CurioInventory(IAttachmentHolder attachmentHolder) {
    this.owner = attachmentHolder instanceof LivingEntity ? (LivingEntity) attachmentHolder : null;
  }

  public LivingEntity getOwner() {
    return this.owner;
  }

  public void resetInventory() {

    if (this.owner != null) {
      Map<ISlotType, ICurioStacksHandler> defaultInventory = this.createDefaultInventory();
      this.curios.clear();

      for (Map.Entry<ISlotType, ICurioStacksHandler> entry : defaultInventory.entrySet()) {
        this.curios.put(entry.getKey().getId(), entry.getValue());
      }
    }
  }

  private Map<ISlotType, ICurioStacksHandler> createDefaultInventory() {
    Map<ISlotType, ICurioStacksHandler> result = new TreeMap<>();
    Map<String, ISlotType> defaultCurios = CuriosSlotTypes.getDefaultEntitySlotTypes(this.owner);

    for (Map.Entry<String, ISlotType> entry : defaultCurios.entrySet()) {
      String id = entry.getKey();
      ISlotType slotType = entry.getValue();
      result.put(slotType, new CurioStacksHandler(this, id, slotType.getSize(),
          slotType.useNativeGui(), slotType.hasCosmetic(), slotType.canToggleRendering(),
          slotType.getDropRule()));
    }
    return result;
  }

  public Set<ICurioStacksHandler> getUpdatingInventories() {
    return this.updates;
  }

  public List<ItemStack> getDroppingStacks() {
    return this.invalidStacks;
  }

  private static final Identifier SIZE_SHIFT = CuriosResources.resource("size_shift");

  public void loadInventoryConfiguration() {
    Map<ISlotType, ICurioStacksHandler> defaultInventory = this.createDefaultInventory();
    SortedMap<ISlotType, ICurioStacksHandler> sortedCurios = new TreeMap<>(defaultInventory);

    for (Map.Entry<String, ICurioStacksHandler> entry : this.curios.entrySet()) {
      String id = entry.getKey();
      ISlotType slotType = CuriosSlotTypes.getSlotType(id, this.owner.level().isClientSide());
      ICurioStacksHandler prevStacksHandler = entry.getValue();

      if (defaultInventory.containsKey(slotType)) {
        ICurioStacksHandler curioStacksHandler = defaultInventory.get(slotType);
        int defaultSize = curioStacksHandler.getSlots();
        int oldSize = prevStacksHandler.getSlots();
        curioStacksHandler.copyModifiers(prevStacksHandler);

        if (oldSize != defaultSize) {
          curioStacksHandler.addTransientModifier(
              new AttributeModifier(SIZE_SHIFT, oldSize - defaultSize,
                  AttributeModifier.Operation.ADD_VALUE));
        }
        int index = 0;

        while (index < curioStacksHandler.getSlots() && index < prevStacksHandler.getSlots()) {
          ItemStack prevStack = prevStacksHandler.getStacks().getStackInSlot(index);

          if (!prevStack.isEmpty()) {

            if (curioStacksHandler.getStacks().isItemValid(index, prevStack)) {
              curioStacksHandler.getStacks().setStackInSlot(index, prevStack);
            } else {
              this.invalidStacks.add(prevStack);
            }
          }
          ItemStack prevCosmetic =
              prevStacksHandler.getCosmeticStacks().getStackInSlot(index);

          if (!prevCosmetic.isEmpty()) {

            if (curioStacksHandler.getStacks().isItemValid(index, prevCosmetic)) {
              curioStacksHandler
                  .getCosmeticStacks()
                  .setStackInSlot(
                      index, prevStacksHandler.getCosmeticStacks().getStackInSlot(index));
            } else {
              this.invalidStacks.add(prevCosmetic);
            }
          }
          index++;
        }

        while (index < prevStacksHandler.getSlots()) {
          this.invalidStacks.add(prevStacksHandler.getStacks().getStackInSlot(index));
          this.invalidStacks.add(prevStacksHandler.getCosmeticStacks().getStackInSlot(index));
          index++;
        }
        sortedCurios.put(slotType, curioStacksHandler);

        for (int j = 0;
             j < curioStacksHandler.getRenders().size()
                 && j < prevStacksHandler.getRenders().size();
             j++) {
          curioStacksHandler.getRenders().set(j, prevStacksHandler.getRenders().get(j));
        }

        for (int j = 0;
             j < curioStacksHandler.getActiveStates().size()
                 && j < prevStacksHandler.getActiveStates().size();
             j++) {
          curioStacksHandler.getActiveStates()
              .set(j, prevStacksHandler.getActiveStates().get(j));
        }
      } else {
        IDynamicStackHandler stackHandler = prevStacksHandler.getStacks();
        IDynamicStackHandler cosmeticStackHandler = prevStacksHandler.getCosmeticStacks();

        for (int j = 0; j < stackHandler.getSlots(); j++) {
          ItemStack stack = stackHandler.getStackInSlot(j);

          if (!stack.isEmpty()) {
            this.invalidStacks.add(stack);
          }

          ItemStack cosmeticStack = cosmeticStackHandler.getStackInSlot(j);

          if (!cosmeticStack.isEmpty()) {
            this.invalidStacks.add(cosmeticStack);
          }
        }
      }
    }
    this.curios.clear();

    for (Map.Entry<ISlotType, ICurioStacksHandler> entry : sortedCurios.entrySet()) {
      ICurioStacksHandler stacksHandler = entry.getValue();

      if (stacksHandler instanceof CurioStacksHandler curioStacksHandler) {
        curioStacksHandler.setDataLoaded();
      }
      this.curios.put(entry.getKey().getId(), stacksHandler);
    }
  }

  public Map<String, ICurioStacksHandler> asMap() {
    return this.curios;
  }

  public void replace(Map<String, ICurioStacksHandler> curios) {
    this.curios.clear();
    this.curios.putAll(curios);
  }

  private static final String CURIOS_KEY = "Curios";

  @Override
  public void serialize(@Nonnull ValueOutput valueOutput) {
    ValueOutput.ValueOutputList list = valueOutput.childrenList(CURIOS_KEY);
    this.curios.forEach((key, stacks) -> {
      ValueOutput output = list.addChild();
      output.putChild(key, stacks);
    });
  }

  @Override
  public void deserialize(@Nonnull ValueInput valueInput) {
    this.curios.clear();
    ValueInput.ValueInputList list = valueInput.childrenListOrEmpty(CURIOS_KEY);

    if (list.isEmpty()) {
      this.resetInventory();
      return;
    }
    list.forEach(input -> {

      for (String id : input.keySet()) {

        if (!id.isEmpty()) {
          ICurioStacksHandler stacks = new CurioStacksHandler(this, id);
          stacks.deserialize(input.childOrEmpty(id));
          this.curios.put(id, stacks);
        }
      }
    });
  }
}
