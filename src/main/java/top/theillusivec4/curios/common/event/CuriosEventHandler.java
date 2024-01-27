/*
 * Copyright (c) 2018-2020 C4
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

package top.theillusivec4.curios.common.event;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.EnderManAngerEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent.PickupXp;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.event.CurioDropsEvent;
import top.theillusivec4.curios.api.event.CurioEquipEvent;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurio.DropRule;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.api.type.util.ICuriosHelper;
import top.theillusivec4.curios.common.CuriosHelper;
import top.theillusivec4.curios.common.capability.CurioInventoryCapability;
import top.theillusivec4.curios.common.capability.CurioItemCapability;
import top.theillusivec4.curios.common.capability.ItemizedCurioCapability;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.SPacketSetIcons;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncCurios;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncModifiers;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack.HandlerType;
import top.theillusivec4.curios.common.util.EquipCurioTrigger;

public class CuriosEventHandler {

  public static boolean dirtyTags = false;

  private static void handleDrops(String identifier, LivingEntity livingEntity,
                                  List<Tuple<Predicate<ItemStack>, DropRule>> dropRules,
                                  NonNullList<Boolean> renders, IDynamicStackHandler stacks,
                                  boolean cosmetic, Collection<ItemEntity> drops,
                                  boolean keepInventory, LivingDropsEvent evt) {
    for (int i = 0; i < stacks.getSlots(); i++) {
      ItemStack stack = stacks.getStackInSlot(i);
      SlotContext slotContext = new SlotContext(identifier, livingEntity, i, cosmetic,
          renders.size() > i && renders.get(i));

      if (!stack.isEmpty()) {
        DropRule dropRuleOverride = null;

        for (Tuple<Predicate<ItemStack>, DropRule> override : dropRules) {

          if (override.getA().test(stack)) {
            dropRuleOverride = override.getB();
          }
        }
        DropRule dropRule = dropRuleOverride != null ? dropRuleOverride :
            CuriosApi.getCuriosHelper().getCurio(stack).map(curio -> curio
                .getDropRule(slotContext, evt.getSource(), evt.getLootingLevel(),
                    evt.isRecentlyHit())).orElse(DropRule.DEFAULT);

        if ((dropRule == DropRule.DEFAULT && keepInventory) || dropRule == DropRule.ALWAYS_KEEP) {
          continue;
        }

        if (!EnchantmentHelper.hasVanishingCurse(stack) && dropRule != DropRule.DESTROY) {
          drops.add(getDroppedItem(stack, livingEntity));
        }
        stacks.setStackInSlot(i, ItemStack.EMPTY);
      }
    }
  }

  private static ItemEntity getDroppedItem(ItemStack droppedItem, LivingEntity livingEntity) {
    double d0 = livingEntity.getY() - 0.30000001192092896D + livingEntity.getEyeHeight();
    ItemEntity entityitem = new ItemEntity(livingEntity.level, livingEntity.getX(), d0,
        livingEntity.getZ(), droppedItem);
    entityitem.setPickUpDelay(40);
    float f = livingEntity.level.random.nextFloat() * 0.5F;
    float f1 = livingEntity.level.random.nextFloat() * ((float) Math.PI * 2F);
    entityitem.setDeltaMovement((-Mth.sin(f1) * f), 0.20000000298023224D, (Mth.cos(f1) * f));
    return entityitem;
  }

  private static boolean handleMending(Player player, IDynamicStackHandler stacks,
                                       PickupXp evt) {

    for (int i = 0; i < stacks.getSlots(); i++) {
      ItemStack stack = stacks.getStackInSlot(i);

      if (!stack.isEmpty() && stack.getEnchantmentLevel(Enchantments.MENDING) > 0 &&
          stack.isDamaged()) {
        evt.setCanceled(true);
        ExperienceOrb orb = evt.getOrb();
        player.takeXpDelay = 2;
        player.take(orb, 1);
        int toRepair = Math.min(orb.value * 2, stack.getDamageValue());
        orb.value -= toRepair / 2;
        stack.setDamageValue(stack.getDamageValue() - toRepair);

        if (orb.value > 0) {
          player.giveExperiencePoints(orb.value);
        }
        orb.remove(Entity.RemovalReason.KILLED);
        return true;
      }
    }
    return false;
  }

  @SubscribeEvent
  public void playerLoggedIn(PlayerLoggedInEvent evt) {
    Player playerEntity = evt.getEntity();

    if (playerEntity instanceof ServerPlayer) {
      Collection<ISlotType> slotTypes = CuriosApi.getSlotHelper().getSlotTypes();
      Map<String, ResourceLocation> icons = new HashMap<>();
      slotTypes.forEach(type -> icons.put(type.getIdentifier(), type.getIcon()));
      NetworkHandler.INSTANCE
          .send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerEntity),
              new SPacketSetIcons(icons));
    }
  }

  @SubscribeEvent
  public void attachEntitiesCapabilities(AttachCapabilitiesEvent<Entity> evt) {

    if (evt.getObject() instanceof Player) {
      evt.addCapability(CuriosCapability.ID_INVENTORY,
          CurioInventoryCapability.createProvider((Player) evt.getObject()));
    }
  }

  /**
   * Handler for registering item's capabilities implemented through IItemCurio interface.
   */

  @SubscribeEvent
  public void attachStackCapabilities(AttachCapabilitiesEvent<ItemStack> evt) {
    ItemStack stack = evt.getObject();

    if (stack.getItem() instanceof ICurioItem itemCurio) {

      if (itemCurio.hasCurioCapability(stack)) {
        ItemizedCurioCapability itemizedCapability = new ItemizedCurioCapability(itemCurio, stack);
        evt.addCapability(CuriosCapability.ID_ITEM,
            CurioItemCapability.createProvider(itemizedCapability));
      }
    }
  }

  @SubscribeEvent
  public void entityJoinWorld(EntityJoinLevelEvent evt) {

    Entity entity = evt.getEntity();

    if (entity instanceof ServerPlayer serverPlayerEntity) {
      CuriosApi.getCuriosHelper().getCuriosHandler(serverPlayerEntity).ifPresent(handler -> {
        ServerPlayer mp = (ServerPlayer) entity;
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> mp),
            new SPacketSyncCurios(mp.getId(), handler.getCurios()));
      });
    }
  }

  @SubscribeEvent
  public void playerStartTracking(PlayerEvent.StartTracking evt) {

    Entity target = evt.getTarget();
    Player player = evt.getEntity();

    if (player instanceof ServerPlayer && target instanceof LivingEntity livingBase) {
      CuriosApi.getCuriosHelper().getCuriosHandler(livingBase).ifPresent(
          handler -> NetworkHandler.INSTANCE
              .send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                  new SPacketSyncCurios(target.getId(), handler.getCurios())));
    }
  }

  @SubscribeEvent
  public void playerClone(PlayerEvent.Clone evt) {
    Player player = evt.getEntity();
    Player oldPlayer = evt.getOriginal();
    oldPlayer.revive();
    LazyOptional<ICuriosItemHandler> oldHandler = CuriosApi.getCuriosHelper()
        .getCuriosHandler(oldPlayer);
    LazyOptional<ICuriosItemHandler> newHandler = CuriosApi.getCuriosHelper()
        .getCuriosHandler(player);
    oldHandler.ifPresent(
        oldCurios -> newHandler.ifPresent(newCurios -> newCurios.readTag(oldCurios.writeTag())));
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void playerDrops(LivingDropsEvent evt) {

    LivingEntity livingEntity = evt.getEntity();

    if (!livingEntity.isSpectator()) {

      CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {
        Collection<ItemEntity> drops = evt.getDrops();
        Collection<ItemEntity> curioDrops = new ArrayList<>();
        Map<String, ICurioStacksHandler> curios = handler.getCurios();

        DropRulesEvent dropRulesEvent = new DropRulesEvent(livingEntity, handler, evt.getSource(),
            evt.getLootingLevel(), evt.isRecentlyHit());
        MinecraftForge.EVENT_BUS.post(dropRulesEvent);
        List<Tuple<Predicate<ItemStack>, DropRule>> dropRules = dropRulesEvent.getOverrides();

        boolean keepInventory = livingEntity.level.getGameRules()
            .getBoolean(GameRules.RULE_KEEPINVENTORY);

        curios.forEach((id, stacksHandler) -> {
          handleDrops(id, livingEntity, dropRules, stacksHandler.getRenders(),
              stacksHandler.getStacks(), false, curioDrops, keepInventory, evt);
          handleDrops(id, livingEntity, dropRules, stacksHandler.getRenders(),
              stacksHandler.getCosmeticStacks(), true, curioDrops, keepInventory, evt);
        });

        if (!MinecraftForge.EVENT_BUS.post(
            new CurioDropsEvent(livingEntity, handler, evt.getSource(), curioDrops,
                evt.getLootingLevel(), evt.isRecentlyHit()))) {
          drops.addAll(curioDrops);
        }
      });
    }
  }

  @SubscribeEvent
  public void playerXPPickUp(PickupXp evt) {
    Player player = evt.getEntity();

    if (!player.level.isClientSide) {
      CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
        Map<String, ICurioStacksHandler> curios = handler.getCurios();
        for (ICurioStacksHandler stacksHandler : curios.values()) {

          if (handleMending(player, stacksHandler.getStacks(), evt) || handleMending(player,
              stacksHandler.getCosmeticStacks(), evt)) {
            return;
          }
        }
      });
    }
  }

  @SubscribeEvent
  public void curioRightClick(PlayerInteractEvent.RightClickItem evt) {
    Player player = evt.getEntity();
    ItemStack stack = evt.getItemStack();
    ICuriosHelper curiosHelper = CuriosApi.getCuriosHelper();
    curiosHelper.getCurio(stack).ifPresent(
        curio -> curiosHelper.getCuriosHandler(player).ifPresent(handler -> {
          Map<String, ICurioStacksHandler> curios = handler.getCurios();

          for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
            IDynamicStackHandler stackHandler = entry.getValue().getStacks();

            for (int i = 0; i < stackHandler.getSlots(); i++) {
              String id = entry.getKey();
              NonNullList<Boolean> renderStates = entry.getValue().getRenders();
              SlotContext slotContext = new SlotContext(id, player, i, false,
                  renderStates.size() > i && renderStates.get(i));
              CurioEquipEvent equipEvent = new CurioEquipEvent(stack, slotContext);
              MinecraftForge.EVENT_BUS.post(equipEvent);
              Event.Result result = equipEvent.getResult();

              if (result == Event.Result.DENY) {
                continue;
              }

              if (result == Event.Result.ALLOW ||
                  (curiosHelper.isStackValid(slotContext, stack) && curio.canEquip(slotContext) &&
                      curio.canEquipFromUse(slotContext))) {
                ItemStack present = stackHandler.getStackInSlot(i);

                if (present.isEmpty()) {
                  stackHandler.setStackInSlot(i, stack.copy());
                  curio.onEquipFromUse(slotContext);

                  if (!player.isCreative()) {
                    int count = stack.getCount();
                    stack.shrink(count);
                  }
                  evt.setCancellationResult(
                      InteractionResult.sidedSuccess(player.level.isClientSide()));
                  evt.setCanceled(true);
                  return;
                }
              }
            }
          }
        }));
  }

  @SubscribeEvent
  public void worldTick(TickEvent.LevelTickEvent evt) {

    if (evt.level instanceof ServerLevel && dirtyTags) {
      PlayerList list = ((ServerLevel) evt.level).getServer().getPlayerList();
      ICuriosHelper curiosHelper = CuriosApi.getCuriosHelper();

      for (ServerPlayer player : list.getPlayers()) {
        curiosHelper.getCuriosHandler(player).ifPresent(handler -> {

          for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
            ICurioStacksHandler stacksHandler = entry.getValue();
            String id = entry.getKey();
            IDynamicStackHandler stacks = stacksHandler.getStacks();
            IDynamicStackHandler cosmeticStacks = stacksHandler.getCosmeticStacks();
            replaceInvalidStacks(curiosHelper, player, id, stacks, false,
                stacksHandler.getRenders());
            replaceInvalidStacks(curiosHelper, player, id, cosmeticStacks, true,
                stacksHandler.getRenders());
          }
        });
      }
      dirtyTags = false;
    }
  }

  private static void replaceInvalidStacks(ICuriosHelper curiosHelper, ServerPlayer player,
                                           String id, IDynamicStackHandler stacks, boolean cosmetic,
                                           NonNullList<Boolean> renders) {
    for (int i = 0; i < stacks.getSlots(); i++) {
      ItemStack stack = stacks.getStackInSlot(i);
      SlotContext slotContext =
          new SlotContext(id, player, i, cosmetic, renders.size() > i && renders.get(i));

      if (!stack.isEmpty() && !canEquip(slotContext, stack)) {
        stacks.setStackInSlot(i, ItemStack.EMPTY);
        ItemHandlerHelper.giveItemToPlayer(player, stack);
      }
    }
  }

  private static boolean canEquip(SlotContext slotContext, ItemStack stack) {
    CurioEquipEvent equipEvent = new CurioEquipEvent(stack, slotContext);
    MinecraftForge.EVENT_BUS.post(equipEvent);
    Event.Result result = equipEvent.getResult();

    if (result == Event.Result.DENY) {
      return false;
    }
    return result == Event.Result.ALLOW ||
        (CuriosApi.getCuriosHelper().isStackValid(slotContext, stack) &&
            CuriosApi.getCuriosHelper().getCurio(stack).map(curio -> curio.canEquip(slotContext))
                .orElse(true));
  }

  @SubscribeEvent
  public void looting(LootingLevelEvent evt) {
    DamageSource source = evt.getDamageSource();

    if (source != null && source.getEntity() instanceof LivingEntity living) {
      evt.setLootingLevel(evt.getLootingLevel() +
          CuriosApi.getCuriosHelper().getCuriosHandler(living).map(handler -> handler
              .getLootingLevel(source, evt.getEntity(), evt.getLootingLevel())).orElse(0));
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onBreakBlock(BlockEvent.BreakEvent evt) {
    Player player = evt.getPlayer();
    AtomicInteger fortuneLevel = new AtomicInteger();
    ICuriosHelper curiosHelper = CuriosApi.getCuriosHelper();
    curiosHelper.getCuriosHandler(player).ifPresent(handler -> {

      for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
        IDynamicStackHandler stacks = entry.getValue().getStacks();

        for (int i = 0; i < stacks.getSlots(); i++) {
          NonNullList<Boolean> renderStates = entry.getValue().getRenders();
          SlotContext slotContext = new SlotContext(entry.getKey(), player, i, false,
              renderStates.size() > i && renderStates.get(i));
          fortuneLevel.addAndGet(curiosHelper.getCurio(stacks.getStackInSlot(i)).map(
                  curio -> curio.getFortuneLevel(slotContext, null))
              .orElse(0));
        }
      }
    });
    ItemStack stack = player.getMainHandItem();
    int bonusLevel = stack.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
    int silklevel = stack.getEnchantmentLevel(Enchantments.SILK_TOUCH);
    LevelAccessor level = evt.getLevel();
    evt.setExpToDrop(evt.getState()
        .getExpDrop(level, level.getRandom(), evt.getPos(), bonusLevel + fortuneLevel.get(),
            silklevel));
  }

  @SubscribeEvent
  public void enderManAnger(final EnderManAngerEvent evt) {
    Player player = evt.getPlayer();
    CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {

      all:
      for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
        IDynamicStackHandler stacks = entry.getValue().getStacks();

        for (int i = 0; i < stacks.getSlots(); i++) {
          final int index = i;
          NonNullList<Boolean> renderStates = entry.getValue().getRenders();
          boolean hasMask =
              CuriosApi.getCuriosHelper().getCurio(stacks.getStackInSlot(i)).map(curio -> curio
                      .isEnderMask(new SlotContext(entry.getKey(), player, index, false,
                          renderStates.size() > index && renderStates.get(index)), evt.getEntity()))
                  .orElse(false);

          if (hasMask) {
            evt.setCanceled(true);
            break all;
          }
        }
      }
    });
  }

  @SubscribeEvent
  public void tick(LivingEvent.LivingTickEvent evt) {
    LivingEntity livingEntity = evt.getEntity();

    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {
      handler.clearCachedSlotModifiers();
      handler.handleInvalidStacks();
      Map<String, ICurioStacksHandler> curios = handler.getCurios();

      for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
        ICurioStacksHandler stacksHandler = entry.getValue();
        String identifier = entry.getKey();
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();
        IDynamicStackHandler cosmeticStackHandler = stacksHandler.getCosmeticStacks();

        for (int i = 0; i < stacksHandler.getSlots(); i++) {
          NonNullList<Boolean> renderStates = stacksHandler.getRenders();
          SlotContext slotContext = new SlotContext(identifier, livingEntity, i, false,
              renderStates.size() > i && renderStates.get(i));
          ItemStack stack = stackHandler.getStackInSlot(i);
          LazyOptional<ICurio> currentCurio = CuriosApi.getCuriosHelper().getCurio(stack);
          final int index = i;

          if (!stack.isEmpty()) {
            stack.inventoryTick(livingEntity.level, livingEntity, -1, false);
            currentCurio.ifPresent(curio -> curio.curioTick(slotContext));

            // todo: Remove in 1.19
            if (livingEntity.level.isClientSide) {
              currentCurio.ifPresent(curio -> curio.curioAnimate(identifier, index, livingEntity));
            }
          }

          if (!livingEntity.level.isClientSide) {
            ItemStack prevStack = stackHandler.getPreviousStackInSlot(i);

            if (!ItemStack.matches(stack, prevStack)) {
              LazyOptional<ICurio> prevCurio = CuriosApi.getCuriosHelper().getCurio(prevStack);
              syncCurios(livingEntity, stack, currentCurio, prevCurio, identifier, index, false,
                  renderStates.size() > index && renderStates.get(index), HandlerType.EQUIPMENT);
              MinecraftForge.EVENT_BUS
                  .post(new CurioChangeEvent(livingEntity, identifier, i, prevStack, stack));
              UUID uuid = UUID.nameUUIDFromBytes((identifier + i).getBytes());

              if (!prevStack.isEmpty()) {
                Multimap<Attribute, AttributeModifier> map =
                    CuriosApi.getCuriosHelper().getAttributeModifiers(slotContext, uuid, prevStack);
                Multimap<String, AttributeModifier> slots = HashMultimap.create();
                Set<CuriosHelper.SlotAttributeWrapper> toRemove = new HashSet<>();

                for (Attribute attribute : map.keySet()) {

                  if (attribute instanceof CuriosHelper.SlotAttributeWrapper wrapper) {
                    slots.putAll(wrapper.identifier, map.get(attribute));
                    toRemove.add(wrapper);
                  }
                }

                for (Attribute attribute : toRemove) {
                  map.removeAll(attribute);
                }
                livingEntity.getAttributes().removeAttributeModifiers(map);
                handler.removeSlotModifiers(slots);
                prevCurio.ifPresent(curio -> curio.onUnequip(slotContext, stack));
              }

              if (!stack.isEmpty()) {
                Multimap<Attribute, AttributeModifier> map =
                    CuriosApi.getCuriosHelper().getAttributeModifiers(slotContext, uuid, stack);
                Multimap<String, AttributeModifier> slots = HashMultimap.create();
                Set<CuriosHelper.SlotAttributeWrapper> toRemove = new HashSet<>();

                for (Attribute attribute : map.keySet()) {

                  if (attribute instanceof CuriosHelper.SlotAttributeWrapper wrapper) {
                    slots.putAll(wrapper.identifier, map.get(attribute));
                    toRemove.add(wrapper);
                  }
                }

                for (Attribute attribute : toRemove) {
                  map.removeAll(attribute);
                }
                livingEntity.getAttributes().addTransientAttributeModifiers(map);
                handler.addTransientSlotModifiers(slots);
                currentCurio.ifPresent(curio -> curio.onEquip(slotContext, prevStack));

                if (livingEntity instanceof ServerPlayer) {
                  EquipCurioTrigger.INSTANCE.trigger(slotContext, (ServerPlayer) livingEntity,
                      stack, (ServerLevel) livingEntity.level, livingEntity.getX(),
                      livingEntity.getY(), livingEntity.getZ());
                }
              }
              stackHandler.setPreviousStackInSlot(i, stack.copy());
            }
            ItemStack cosmeticStack = cosmeticStackHandler.getStackInSlot(i);
            ItemStack prevCosmeticStack = cosmeticStackHandler.getPreviousStackInSlot(i);

            if (!ItemStack.matches(cosmeticStack, prevCosmeticStack)) {
              syncCurios(livingEntity, cosmeticStack,
                  CuriosApi.getCuriosHelper().getCurio(cosmeticStack),
                  CuriosApi.getCuriosHelper().getCurio(prevCosmeticStack), identifier, index, true,
                  true, HandlerType.COSMETIC);
              cosmeticStackHandler.setPreviousStackInSlot(index, cosmeticStack.copy());
            }
            Set<ICurioStacksHandler> updates = handler.getUpdatingInventories();

            if (!updates.isEmpty()) {
              NetworkHandler.INSTANCE.send(
                  PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> livingEntity),
                  new SPacketSyncModifiers(livingEntity.getId(), updates));
              updates.clear();
            }
          }
        }
      }
    });
  }

  private static void syncCurios(LivingEntity livingEntity, ItemStack stack,
                                 LazyOptional<ICurio> currentCurio, LazyOptional<ICurio> prevCurio,
                                 String identifier, int index, boolean cosmetic, boolean visible,
                                 HandlerType type) {
    SlotContext slotContext = new SlotContext(identifier, livingEntity, index, cosmetic, visible);
    boolean syncable = currentCurio.map(curio -> curio.canSync(slotContext)).orElse(false) ||
        prevCurio.map(curio -> curio.canSync(slotContext)).orElse(false);
    CompoundTag syncTag = syncable ? currentCurio.map(curio -> {
      CompoundTag tag = curio.writeSyncData(slotContext);
      return tag != null ? tag : new CompoundTag();
    }).orElse(new CompoundTag()) : new CompoundTag();
    NetworkHandler.INSTANCE
        .send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> livingEntity),
            new SPacketSyncStack(livingEntity.getId(), identifier, index, stack, type,
                syncTag));
  }
}
