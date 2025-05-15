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

package top.theillusivec4.curios.common;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.EnderManAngerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.event.CurioDropsEvent;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.ICuriosMenu;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.data.CuriosSlotResources;
import top.theillusivec4.curios.common.inventory.container.CuriosMenu;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncCurios;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncData;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncModifiers;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack.HandlerType;
import top.theillusivec4.curios.config.CuriosConfig;
import top.theillusivec4.curios.impl.CuriosRegistry;

public class CuriosCommonEvents {

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
        DropRule dropRule = dropRuleOverride != null ? dropRuleOverride : CuriosApi.getCurio(stack)
            .map(curio -> curio.getDropRule(slotContext, evt.getSource(), evt.isRecentlyHit()))
            .orElse(DropRule.DEFAULT);

        if (dropRule == DropRule.DEFAULT) {
          ISlotType slotType = ISlotType.get(identifier);

          if (slotType != null) {
            dropRule = slotType.getDropRule();
          }
        }

        if ((dropRule == DropRule.DEFAULT && keepInventory) || dropRule == DropRule.ALWAYS_KEEP) {
          continue;
        }

        if (!EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP) &&
            dropRule != DropRule.DESTROY) {
          drops.add(getDroppedItem(stack, livingEntity));
        }
        stacks.setStackInSlot(i, ItemStack.EMPTY);
      }
    }
  }

  private static ItemEntity getDroppedItem(ItemStack droppedItem, LivingEntity livingEntity) {
    double d0 = livingEntity.getY() - 0.30000001192092896D + livingEntity.getEyeHeight();
    ItemEntity entityitem = new ItemEntity(livingEntity.level(), livingEntity.getX(), d0,
                                           livingEntity.getZ(), droppedItem);
    entityitem.setPickUpDelay(40);
    float f = livingEntity.level().random.nextFloat() * 0.5F;
    float f1 = livingEntity.level().random.nextFloat() * ((float) Math.PI * 2F);
    entityitem.setDeltaMovement((-Mth.sin(f1) * f), 0.20000000298023224D, (Mth.cos(f1) * f));
    return entityitem;
  }

  private static boolean handleMending(Player player, IDynamicStackHandler stacks,
                                       PlayerXpEvent.PickupXp evt) {
    Holder<Enchantment> mendingHolder =
        player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(Enchantments.MENDING);
    for (int i = 0; i < stacks.getSlots(); i++) {
      ItemStack stack = stacks.getStackInSlot(i);

      if (!stack.isEmpty() && stack.getEnchantmentLevel(mendingHolder) > 0 && stack.isDamaged()) {
        evt.setCanceled(true);
        ExperienceOrb orb = evt.getOrb();
        player.takeXpDelay = 2;
        player.take(orb, 1);
        int value = orb.getValue();
        int toRepair = Math.min(value * 2, stack.getDamageValue());
        value -= toRepair / 2;
        stack.setDamageValue(stack.getDamageValue() - toRepair);

        if (value > 0) {
          player.giveExperiencePoints(value);
        }
        orb.remove(Entity.RemovalReason.KILLED);
        return true;
      }
    }
    return false;
  }

  @SubscribeEvent
  public void onTagsUpdated(final ServerAboutToStartEvent evt) {
    CuriosSlotResources.SERVER.populateData();
  }

  @SubscribeEvent
  public void onDatapackSync(OnDatapackSyncEvent evt) {

    if (evt.getPlayer() == null) {
      PlayerList playerList = evt.getPlayerList();

      for (ServerPlayer player : playerList.getPlayers()) {
        SPacketSyncData.send(player);
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
          Tag tag = handler.writeTag();

          for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
            ICurioStacksHandler stacks = entry.getValue();

            for (int i = 0; i < stacks.getSlots(); i++) {
              stacks.getStacks().setStackInSlot(i, ItemStack.EMPTY);
              stacks.getCosmeticStacks().setStackInSlot(i, ItemStack.EMPTY);
            }
          }
          handler.readTag(tag);
          PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                                                               new SPacketSyncCurios(player.getId(),
                                                                                     handler.getCurios()));

          if (player.containerMenu instanceof ICuriosMenu curiosContainer) {
            curiosContainer.resetSlots();
          }
        });
      }
    } else {
      ServerPlayer mp = evt.getPlayer();
      SPacketSyncData.send(mp);
      CuriosApi.getCuriosInventory(mp).ifPresent(
          handler -> {
            Tag tag = handler.writeTag();

            for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
              ICurioStacksHandler stacks = entry.getValue();

              for (int i = 0; i < stacks.getSlots(); i++) {
                stacks.getStacks().setStackInSlot(i, ItemStack.EMPTY);
                stacks.getCosmeticStacks().setStackInSlot(i, ItemStack.EMPTY);
              }
            }
            handler.readTag(tag);
            PacketDistributor.sendToPlayer(mp,
                                           new SPacketSyncCurios(mp.getId(), handler.getCurios()));

            if (mp.containerMenu instanceof ICuriosMenu curiosContainer) {
              curiosContainer.resetSlots();
            }
          });
    }
  }

  @SubscribeEvent
  public void entityConstructing(EntityEvent.EntityConstructing evt) {
    Entity entity = evt.getEntity();

    if (entity instanceof LivingEntity livingEntity) {
      CuriosApi.getCuriosInventory(livingEntity).ifPresent(inv -> {
        Tag tag = inv.writeTag();

        for (Map.Entry<String, ICurioStacksHandler> entry : inv.getCurios().entrySet()) {
          ICurioStacksHandler stacks = entry.getValue();

          for (int i = 0; i < stacks.getSlots(); i++) {
            stacks.getStacks().setStackInSlot(i, ItemStack.EMPTY);
            stacks.getCosmeticStacks().setStackInSlot(i, ItemStack.EMPTY);
          }
        }
        inv.readTag(tag);
      });
    }
  }

  @SubscribeEvent
  public void entityJoinWorld(EntityJoinLevelEvent evt) {
    Entity entity = evt.getEntity();

    if (entity instanceof ServerPlayer serverPlayerEntity) {
      CuriosApi.getCuriosInventory(serverPlayerEntity).ifPresent(handler -> {
        ServerPlayer mp = (ServerPlayer) entity;
        PacketDistributor.sendToPlayer(mp, new SPacketSyncCurios(mp.getId(), handler.getCurios()));
      });
    }
  }

  @SubscribeEvent
  public void playerStartTracking(PlayerEvent.StartTracking evt) {
    Entity target = evt.getTarget();
    Player player = evt.getEntity();

    if (player instanceof ServerPlayer serverPlayer && target instanceof LivingEntity livingBase) {
      CuriosApi.getCuriosInventory(livingBase).ifPresent(
          handler -> PacketDistributor.sendToPlayer(serverPlayer,
                                                    new SPacketSyncCurios(target.getId(),
                                                                          handler.getCurios())));
    }
  }

  @SubscribeEvent
  public void playerClone(PlayerEvent.Clone evt) {
    Player player = evt.getEntity();
    Player oldPlayer = evt.getOriginal();
    Optional<ICuriosItemHandler> oldHandler = CuriosApi.getCuriosInventory(oldPlayer);
    Optional<ICuriosItemHandler> newHandler = CuriosApi.getCuriosInventory(player);
    oldHandler.ifPresent(
        oldCurios -> newHandler.ifPresent(newCurios -> newCurios.readTag(oldCurios.writeTag())));
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void playerDrops(LivingDropsEvent evt) {
    LivingEntity livingEntity = evt.getEntity();

    if (!livingEntity.isSpectator()) {

      CuriosApi.getCuriosInventory(livingEntity).ifPresent(handler -> {
        Collection<ItemEntity> drops = evt.getDrops();
        Collection<ItemEntity> curioDrops = new ArrayList<>();
        Map<String, ICurioStacksHandler> curios = handler.getCurios();
        // todo: Fix looting levels when NeoForge has a new API or figure a workaround
        DropRulesEvent dropRulesEvent =
            new DropRulesEvent(livingEntity, handler, evt.getSource(), 0, evt.isRecentlyHit());
        NeoForge.EVENT_BUS.post(dropRulesEvent);
        List<Tuple<Predicate<ItemStack>, DropRule>> dropRules = dropRulesEvent.getOverrides();
        boolean keepInventory = false;

        if (livingEntity instanceof Player
            && livingEntity.level() instanceof ServerLevel serverLevel) {
          keepInventory =
              serverLevel.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);

          if (CuriosConfig.SERVER.keepCurios.get() != CuriosConfig.KeepCurios.DEFAULT) {
            keepInventory = CuriosConfig.SERVER.keepCurios.get() == CuriosConfig.KeepCurios.ON;
          }
        }
        boolean finalKeepInventory = keepInventory;
        curios.forEach((id, stacksHandler) -> {
          handleDrops(id, livingEntity, dropRules, stacksHandler.getRenders(),
                      stacksHandler.getStacks(), false, curioDrops, finalKeepInventory, evt);
          handleDrops(id, livingEntity, dropRules, stacksHandler.getRenders(),
                      stacksHandler.getCosmeticStacks(), true, curioDrops, finalKeepInventory, evt);
        });
        CurioDropsEvent dropsEvent = NeoForge.EVENT_BUS.post(
            new CurioDropsEvent(livingEntity, handler, evt.getSource(), curioDrops, 0,
                                evt.isRecentlyHit()));

        if (!dropsEvent.isCanceled()) {
          drops.addAll(curioDrops);
        }
      });
    }
  }

  @SubscribeEvent
  public void playerXPPickUp(PlayerXpEvent.PickupXp evt) {
    Player player = evt.getEntity();

    if (!player.level().isClientSide) {
      CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
        Map<String, ICurioStacksHandler> curios = handler.getCurios();
        for (ICurioStacksHandler stacksHandler : curios.values()) {

          if (handleMending(player, stacksHandler.getStacks(), evt) || handleMending(player,
                                                                                     stacksHandler.getCosmeticStacks(),
                                                                                     evt)) {
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
    CuriosApi.getCurio(stack).ifPresent(
        curio -> CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
          Map<String, ICurioStacksHandler> curios = handler.getCurios();
          Tuple<IDynamicStackHandler, SlotContext> firstSlot = null;

          for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
            IDynamicStackHandler stackHandler = entry.getValue().getStacks();
            NonNullList<Boolean> activeStates = entry.getValue().getActiveStates();

            for (int i = 0; i < stackHandler.getSlots(); i++) {
              boolean active = activeStates.size() > i && activeStates.get(i);

              if (!active) {
                continue;
              }
              String id = entry.getKey();
              NonNullList<Boolean> renderStates = entry.getValue().getRenders();
              SlotContext slotContext = new SlotContext(id, player, i, false,
                                                        renderStates.size() > i && renderStates.get(
                                                            i));

              if (stackHandler.isItemValid(i, stack) && curio.canEquipFromUse(slotContext)) {
                ItemStack present = stackHandler.getStackInSlot(i);

                if (present.isEmpty()) {
                  stackHandler.setStackInSlot(i, stack.copy());
                  curio.onEquipFromUse(slotContext);

                  if (!player.isCreative()) {
                    int count = stack.getCount();
                    stack.shrink(count);
                  }
                  evt.setCancellationResult(
                      player.level().isClientSide() ? InteractionResult.SUCCESS :
                      InteractionResult.SUCCESS_SERVER);
                  evt.setCanceled(true);
                  return;
                } else if (firstSlot == null) {

                  if (stackHandler.extractItem(i, stack.getMaxStackSize(), true).getCount() ==
                      stack.getCount()) {
                    firstSlot = new Tuple<>(stackHandler, slotContext);
                  }
                }
              }
            }
          }

          if (firstSlot != null) {
            IDynamicStackHandler stackHandler = firstSlot.getA();
            SlotContext slotContext = firstSlot.getB();
            int i = slotContext.index();
            ItemStack present = stackHandler.getStackInSlot(i);
            stackHandler.setStackInSlot(i, stack.copy());
            curio.onEquipFromUse(slotContext);
            player.setItemInHand(evt.getHand(), present.copy());
            evt.setCancellationResult(
                player.level().isClientSide() ? InteractionResult.SUCCESS :
                InteractionResult.SUCCESS_SERVER);
            evt.setCanceled(true);
          }
        }));
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onBreakBlock(BlockDropsEvent event) {
    AtomicInteger experience = new AtomicInteger(event.getDroppedExperience());

    if (experience.get() <= 0 || !(event.getBreaker() instanceof LivingEntity entity)) {
      return;
    }

    CuriosApi.getCuriosInventory(entity).ifPresent(handler -> {
      for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
        IDynamicStackHandler stacks = entry.getValue().getStacks();
        NonNullList<Boolean> renderStates = entry.getValue().getRenders();

        for (int i = 0; i < stacks.getSlots(); i++) {
          SlotContext context = new SlotContext(entry.getKey(), entity, i, false,
                                                renderStates.size() > i && renderStates.get(i));

          experience.addAndGet(
              EnchantmentHelper.processBlockExperience(event.getLevel(), event.getTool(),
                                                       CuriosApi.getCurio(stacks.getStackInSlot(i))
                                                           .map(curio -> curio.getFortuneLevel(
                                                               context, null)).orElse(0)));
        }
      }
    });

    event.setDroppedExperience(experience.get());
  }

  static Map<UUID, Pair<Long, Boolean>> enderManMaskCache = new HashMap<>();

  @SubscribeEvent
  public void enderManAnger(final EnderManAngerEvent evt) {
    // Check cached value first
    if (enderManMaskCache.size() > 500) {
      enderManMaskCache.clear();
    }
    Player player = evt.getPlayer();
    long gameTime = player.level().getGameTime();

    if (enderManMaskCache.containsKey(player.getUUID())) {
      var pair = enderManMaskCache.get(player.getUUID());

      if (pair.getFirst() == gameTime) {
        evt.setCanceled(pair.getSecond());
        return;
      }
    }
    CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
      all:
      for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
        IDynamicStackHandler stacks = entry.getValue().getStacks();

        for (int i = 0; i < stacks.getSlots(); i++) {
          final int index = i;
          NonNullList<Boolean> renderStates = entry.getValue().getRenders();
          boolean hasMask = CuriosApi.getCurio(stacks.getStackInSlot(i)).map(
                  curio -> curio.isEnderMask(new SlotContext(entry.getKey(), player, index, false,
                                                             renderStates.size() > index
                                                                 && renderStates.get(index)),
                                             evt.getEntity()))
              .orElse(false);

          if (hasMask) {
            enderManMaskCache.put(player.getUUID(), Pair.of(gameTime, true));
            evt.setCanceled(true);
            break all;
          }
        }
      }
    });
    enderManMaskCache.put(player.getUUID(), Pair.of(gameTime, false));
  }

  @SubscribeEvent
  public void tick(EntityTickEvent.Post evt) {
    Entity entity = evt.getEntity();

    if (entity instanceof LivingEntity livingEntity) {
      if (livingEntity instanceof Player player &&
          player.containerMenu instanceof CuriosMenu curiosMenu) {
        curiosMenu.checkQuickMove();
      }

      CuriosApi.getCuriosInventory(livingEntity).ifPresent(handler -> {
        handler.clearCachedSlotModifiers();
        handler.handleInvalidStacks();
        Map<String, ICurioStacksHandler> curios = handler.getCurios();

        for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
          ICurioStacksHandler stacksHandler = entry.getValue();
          String identifier = entry.getKey();
          IDynamicStackHandler stackHandler = stacksHandler.getStacks();
          IDynamicStackHandler cosmeticStackHandler = stacksHandler.getCosmeticStacks();
          NonNullList<Boolean> renderStates = stacksHandler.getRenders();

          for (int i = 0; i < stacksHandler.getSlots(); i++) {
            stacksHandler.updateActiveState(i);
            NonNullList<Boolean> activeStates = stacksHandler.getActiveStates();
            boolean functional = activeStates.size() > i && activeStates.get(i);
            SlotContext slotContext = new SlotContext(identifier, livingEntity, i, false,
                                                      renderStates.size() > i && renderStates.get(
                                                          i));
            ItemStack stack = stackHandler.getStackInSlot(i);
            Optional<ICurio> currentCurio = CuriosApi.getCurio(stack);

            if (functional && !stack.isEmpty()) {
              stack.inventoryTick(livingEntity.level(), livingEntity, null);
              currentCurio.ifPresent(curio -> curio.curioTick(slotContext));
            }

            if (!livingEntity.level().isClientSide) {
              ItemStack prevStack = stackHandler.getPreviousStackInSlot(i);

              if (!ItemStack.matches(stack, prevStack)) {
                boolean flag = false;
                Optional<ICurio> prevCurio = CuriosApi.getCurio(prevStack);
                syncCurios(livingEntity, stack, currentCurio, prevCurio, identifier, i, false,
                           renderStates.size() > i && renderStates.get(i), HandlerType.EQUIPMENT);

                if (functional) {
                  CurioChangeEvent changeEvent;

                  if (ItemStack.isSameItem(stack, prevStack)) {
                    flag = true;
                    changeEvent =
                        new CurioChangeEvent.State(livingEntity, identifier, i, prevStack, stack);
                  } else {
                    changeEvent =
                        new CurioChangeEvent.Item(livingEntity, identifier, i, prevStack, stack);
                  }
                  NeoForge.EVENT_BUS.post(changeEvent);
                  AttributeMap attributeMap = livingEntity.getAttributes();
                  final boolean isStateChange = flag;

                  if (!prevStack.isEmpty()) {
                    ICurioItem
                        .forEachModifier(prevStack, slotContext,
                                         (attributeHolder, attributeModifier) -> {
                                           if (attributeHolder.value() instanceof SlotAttribute slotAttribute) {
                                             handler.removeSlotModifier(
                                                 slotAttribute.id(),
                                                 attributeModifier.id());
                                           } else {
                                             AttributeInstance instance =
                                                 attributeMap.getInstance(attributeHolder);

                                             if (instance != null) {
                                               instance.removeModifier(attributeModifier);
                                             }
                                           }
                                         });
                    prevCurio.ifPresent(curio -> {

                      if (!isStateChange) {
                        curio.onUnequip(slotContext, stack);
                      }
                    });
                  }

                  if (!stack.isEmpty()) {
                    ICurioItem
                        .forEachModifier(stack, slotContext,
                                         (attributeHolder, attributeModifier) -> {
                                           if (attributeHolder.value() instanceof SlotAttribute slotAttribute) {
                                             handler.addTransientSlotModifier(
                                                 slotAttribute.id(),
                                                 attributeModifier.id(), attributeModifier.amount(),
                                                 attributeModifier.operation());
                                           } else {
                                             AttributeInstance instance =
                                                 attributeMap.getInstance(attributeHolder);

                                             if (instance != null) {
                                               instance.addOrUpdateTransientModifier(
                                                   attributeModifier);
                                             }
                                           }
                                         });
                    currentCurio.ifPresent(curio -> {

                      if (isStateChange) {
                        curio.onStateChange(slotContext, prevStack);
                      } else {
                        curio.onEquip(slotContext, prevStack);
                      }
                    });

                    if (livingEntity instanceof ServerPlayer) {
                      CuriosRegistry.EQUIP_TRIGGER.get()
                          .trigger(slotContext, (ServerPlayer) livingEntity, stack);
                    }
                  }
                }
                stackHandler.setPreviousStackInSlot(i, stack.copy());
              }
              ItemStack cosmeticStack = cosmeticStackHandler.getStackInSlot(i);
              ItemStack prevCosmeticStack = cosmeticStackHandler.getPreviousStackInSlot(i);

              if (!ItemStack.matches(cosmeticStack, prevCosmeticStack)) {
                syncCurios(livingEntity, cosmeticStack, CuriosApi.getCurio(cosmeticStack),
                           CuriosApi.getCurio(prevCosmeticStack), identifier, i, true, true,
                           HandlerType.COSMETIC);
                cosmeticStackHandler.setPreviousStackInSlot(i, cosmeticStack.copy());
              }
            }
          }
        }

        if (!livingEntity.level().isClientSide()) {
          Set<ICurioStacksHandler> updates = handler.getUpdatingInventories();

          if (!updates.isEmpty()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(livingEntity,
                                                                 new SPacketSyncModifiers(
                                                                     livingEntity.getId(),
                                                                     updates));
            updates.clear();
          }
        }
      });
    }
  }

  @SubscribeEvent
  public void livingEquipmentChange(final LivingEquipmentChangeEvent evt) {
    CuriosApi.getCuriosInventory(evt.getEntity()).ifPresent(inv -> {
      ItemStack from = evt.getFrom();
      ItemStack to = evt.getTo();
      EquipmentSlot slot = evt.getSlot();

      if (!from.isEmpty()) {
        Multimap<String, AttributeModifier> slots = HashMultimap.create();
        from.forEachModifier(slot, (att, modifier) -> {
          if (att.value() instanceof SlotAttribute wrapper) {
            slots.putAll(wrapper.id(), Collections.singleton(modifier));
          }
        });
        inv.removeSlotModifiers(slots);
      }

      if (!to.isEmpty()) {
        Multimap<String, AttributeModifier> slots = HashMultimap.create();
        to.forEachModifier(slot, (att, modifier) -> {
          if (att.value() instanceof SlotAttribute wrapper) {
            slots.putAll(wrapper.id(), Collections.singleton(modifier));
          }
        });
        inv.addTransientSlotModifiers(slots);
      }
    });
  }

  private static void syncCurios(LivingEntity livingEntity, ItemStack stack,
                                 Optional<ICurio> currentCurio, Optional<ICurio> prevCurio,
                                 String identifier, int index, boolean cosmetic, boolean visible,
                                 HandlerType type) {
    SlotContext slotContext = new SlotContext(identifier, livingEntity, index, cosmetic, visible);
    boolean syncable = currentCurio.map(curio -> curio.canSync(slotContext)).orElse(false) ||
        prevCurio.map(curio -> curio.canSync(slotContext)).orElse(false);
    CompoundTag syncTag = syncable ?
                          currentCurio.map(curio -> curio.writeSyncData(slotContext))
                              .orElse(new CompoundTag()) :
                          new CompoundTag();
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(livingEntity,
                                                         new SPacketSyncStack(livingEntity.getId(),
                                                                              identifier, index,
                                                                              stack, type.ordinal(),
                                                                              syncTag));
  }
}
