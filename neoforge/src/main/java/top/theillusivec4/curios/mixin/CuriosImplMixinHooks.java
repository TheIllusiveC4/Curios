/*
 * Copyright (c) 2018-2023 C4
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
 */

package top.theillusivec4.curios.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.common.data.CuriosEntityManager;
import top.theillusivec4.curios.common.data.CuriosSlotManager;
import top.theillusivec4.curios.common.network.server.SPacketBreak;
import top.theillusivec4.curios.common.slottype.SlotType;

public class CuriosImplMixinHooks {

  private static final Map<Item, ICurioItem> REGISTRY = new ConcurrentHashMap<>();

  public static void registerCurio(Item item, ICurioItem icurio) {
    REGISTRY.put(item, icurio);
  }

  public static Optional<ICurioItem> getCurioFromRegistry(Item item) {
    return Optional.ofNullable(REGISTRY.get(item));
  }

  public static Optional<ISlotType> getSlot(String id) {
    return Optional.ofNullable(CuriosApi.getSlots().get(id));
  }

  public static ResourceLocation getSlotIcon(String id) {
    return CuriosSlotManager.INSTANCE.getIcon(id);
  }

  public static Map<String, ISlotType> getSlots() {
    return CuriosSlotManager.INSTANCE.getSlots();
  }

  public static Map<String, ISlotType> getPlayerSlots() {
    return CuriosApi.getEntitySlots(EntityType.PLAYER);
  }

  public static Map<String, ISlotType> getEntitySlots(EntityType<?> type) {
    return CuriosEntityManager.INSTANCE.getEntitySlots(type);
  }

  public static Map<String, ISlotType> getItemStackSlots(ItemStack stack) {
    Map<String, ISlotType> result = new HashMap<>();
    Set<String> ids = stack.getTags()
        .filter(tagKey -> tagKey.location().getNamespace().equals(CuriosApi.MODID))
        .map(tagKey -> tagKey.location().getPath()).collect(Collectors.toSet());
    Map<String, ISlotType> allSlots = CuriosSlotManager.INSTANCE.getSlots();

    for (String id : ids) {
      ISlotType slotType = allSlots.get(id);

      if (slotType != null) {
        result.put(id, slotType);
      } else {
        result.put(id, new SlotType.Builder(id).build());
      }
    }
    return result;
  }

  public static Map<String, ISlotType> getItemStackSlots(ItemStack stack,
                                                         LivingEntity livingEntity) {
    Map<String, ISlotType> result = new HashMap<>();
    Set<String> ids = stack.getTags()
        .filter(tagKey -> tagKey.location().getNamespace().equals(CuriosApi.MODID))
        .map(tagKey -> tagKey.location().getPath()).collect(Collectors.toSet());
    Map<String, ISlotType> entitySlots = getEntitySlots(livingEntity.getType());

    for (String id : ids) {
      ISlotType slotType = entitySlots.get(id);

      if (slotType != null) {
        result.put(id, slotType);
      } else {
        result.put(id, new SlotType.Builder(id).build());
      }
    }
    return result;
  }

  public static Optional<ICurio> getCurio(ItemStack stack) {
    return Optional.ofNullable(stack.getCapability(CuriosCapability.ITEM));
  }

  public static Optional<ICuriosItemHandler> getCuriosInventory(LivingEntity livingEntity) {

    if (livingEntity != null) {
      return Optional.ofNullable(livingEntity.getCapability(CuriosCapability.INVENTORY));
    } else {
      return Optional.empty();
    }
  }

  public static boolean isStackValid(SlotContext slotContext, ItemStack stack) {
    String id = slotContext.identifier();
    Set<String> slots = getItemStackSlots(stack).keySet();
    return (!slots.isEmpty() && id.equals("curio")) || slots.contains(id) ||
        slots.contains("curio");
  }

  public static Multimap<Attribute, AttributeModifier> getAttributeModifiers(
      SlotContext slotContext, UUID uuid, ItemStack stack) {
    Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();

    if (stack.getTag() != null && stack.getTag().contains("CurioAttributeModifiers", 9)) {
      ListTag listnbt = stack.getTag().getList("CurioAttributeModifiers", 10);
      String identifier = slotContext.identifier();

      for (int i = 0; i < listnbt.size(); ++i) {
        CompoundTag compoundnbt = listnbt.getCompound(i);

        if (compoundnbt.getString("Slot").equals(identifier)) {
          ResourceLocation rl = ResourceLocation.tryParse(compoundnbt.getString("AttributeName"));
          UUID id = uuid;

          if (rl != null) {

            if (compoundnbt.contains("UUID")) {
              id = compoundnbt.getUUID("UUID");
            }

            if (id.getLeastSignificantBits() != 0L && id.getMostSignificantBits() != 0L) {
              AttributeModifier.Operation operation =
                  AttributeModifier.Operation.fromValue(compoundnbt.getInt("Operation"));
              double amount = compoundnbt.getDouble("Amount");
              String name = compoundnbt.getString("Name");

              if (rl.getNamespace().equals("curios")) {
                String identifier1 = rl.getPath();

                if (CuriosApi.getSlot(identifier1).isPresent()) {
                  CuriosApi.addSlotModifier(multimap, identifier1, id, amount, operation);
                }
              } else {
                Attribute attribute = BuiltInRegistries.ATTRIBUTE.getOptional(rl).orElse(null);

                if (attribute != null) {
                  multimap.put(attribute, new AttributeModifier(id, name, amount, operation));
                }
              }
            }
          }
        }
      }
    } else {
      multimap = getCurio(stack).map(curio -> curio.getAttributeModifiers(slotContext, uuid))
          .orElse(multimap);
    }
    CurioAttributeModifierEvent evt =
        new CurioAttributeModifierEvent(stack, slotContext, uuid, multimap);
    NeoForge.EVENT_BUS.post(evt);
    return LinkedHashMultimap.create(evt.getModifiers());
  }

  public static void addSlotModifier(Multimap<Attribute, AttributeModifier> map, String identifier,
                                     UUID uuid, double amount,
                                     AttributeModifier.Operation operation) {
    map.put(SlotAttribute.getOrCreate(identifier),
        new AttributeModifier(uuid, identifier, amount, operation));
  }

  public static void addSlotModifier(ItemStack stack, String identifier, String name, UUID uuid,
                                     double amount, AttributeModifier.Operation operation,
                                     String slot) {
    addModifier(stack, SlotAttribute.getOrCreate(identifier), name, uuid, amount, operation, slot);
  }

  public static void addModifier(ItemStack stack, Attribute attribute, String name, UUID uuid,
                                 double amount, AttributeModifier.Operation operation,
                                 String slot) {
    CompoundTag tag = stack.getOrCreateTag();

    if (!tag.contains("CurioAttributeModifiers", 9)) {
      tag.put("CurioAttributeModifiers", new ListTag());
    }
    ListTag listtag = tag.getList("CurioAttributeModifiers", 10);
    CompoundTag compoundtag = new CompoundTag();
    compoundtag.putString("Name", name);
    compoundtag.putDouble("Amount", amount);
    compoundtag.putInt("Operation", operation.toValue());

    if (uuid != null) {
      compoundtag.putUUID("UUID", uuid);
    }
    String id = "";

    if (attribute instanceof SlotAttribute wrapper) {
      id = "curios:" + wrapper.getIdentifier();
    } else {
      ResourceLocation rl = BuiltInRegistries.ATTRIBUTE.getKey(attribute);

      if (rl != null) {
        id = rl.toString();
      }
    }

    if (!id.isEmpty()) {
      compoundtag.putString("AttributeName", id);
    }
    compoundtag.putString("Slot", slot);
    listtag.add(compoundtag);
  }

  public static void broadcastCurioBreakEvent(SlotContext slotContext) {
    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(slotContext.entity()).send(
        new SPacketBreak(slotContext.entity().getId(), slotContext.identifier(),
            slotContext.index()));
  }
}
