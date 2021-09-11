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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent.PickupXp;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.ItemHandlerHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.event.CurioDropsEvent;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurio.DropRule;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.api.type.util.ICuriosHelper;
import top.theillusivec4.curios.common.capability.CurioInventoryCapability;
import top.theillusivec4.curios.common.capability.CurioItemCapability;
import top.theillusivec4.curios.common.capability.ItemizedCurioCapability;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.SPacketSetIcons;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncCurios;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack.HandlerType;
import top.theillusivec4.curios.common.triggers.EquipCurioTrigger;

public class CuriosEventHandler {

  public static boolean dirtyTags = false;

  private static void handleDrops(LivingEntity livingEntity,
                                  List<Tuple<Predicate<ItemStack>, DropRule>> dropRules,
                                  IDynamicStackHandler stacks, Collection<ItemEntity> drops,
                                  boolean keepInventory) {
    for (int i = 0; i < stacks.getSlots(); i++) {
      ItemStack stack = stacks.getStackInSlot(i);

      if (!stack.isEmpty()) {
        DropRule dropRuleOverride = null;

        for (Tuple<Predicate<ItemStack>, DropRule> override : dropRules) {

          if (override.getA().test(stack)) {
            dropRuleOverride = override.getB();
          }
        }
        DropRule dropRule = dropRuleOverride != null ? dropRuleOverride
            : CuriosApi.getCuriosHelper().getCurio(stack)
            .map(curio -> curio.getDropRule(livingEntity)).orElse(DropRule.DEFAULT);

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
    double d0 = livingEntity.getPosY() - 0.30000001192092896D + livingEntity.getEyeHeight();
    ItemEntity entityitem = new ItemEntity(livingEntity.world, livingEntity.getPosX(), d0,
        livingEntity.getPosZ(), droppedItem);
    entityitem.setPickupDelay(40);
    float f = livingEntity.world.rand.nextFloat() * 0.5F;
    float f1 = livingEntity.world.rand.nextFloat() * ((float) Math.PI * 2F);
    entityitem.setMotion((-MathHelper.sin(f1) * f), 0.20000000298023224D, (MathHelper.cos(f1) * f));
    return entityitem;
  }

  private static boolean handleMending(PlayerEntity player, IDynamicStackHandler stacks,
                                       PickupXp evt) {

    for (int i = 0; i < stacks.getSlots(); i++) {
      ItemStack stack = stacks.getStackInSlot(i);

      if (!stack.isEmpty() && EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack) > 0
          && stack.isDamaged()) {
        evt.setCanceled(true);
        ExperienceOrbEntity orb = evt.getOrb();
        player.xpCooldown = 2;
        player.onItemPickup(orb, 1);
        int toRepair = Math.min(orb.xpValue * 2, stack.getDamage());
        orb.xpValue -= toRepair / 2;
        stack.setDamage(stack.getDamage() - toRepair);

        if (orb.xpValue > 0) {
          player.giveExperiencePoints(orb.xpValue);
        }
        orb.remove();
        return true;
      }
    }
    return false;
  }

  @SubscribeEvent
  public void playerLoggedIn(PlayerLoggedInEvent evt) {
    PlayerEntity playerEntity = evt.getPlayer();

    if (playerEntity instanceof ServerPlayerEntity) {
      Collection<ISlotType> slotTypes = CuriosApi.getSlotHelper().getSlotTypes();
      Map<String, ResourceLocation> icons = new HashMap<>();
      slotTypes.forEach(type -> icons.put(type.getIdentifier(), type.getIcon()));
      NetworkHandler.INSTANCE
          .send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerEntity),
              new SPacketSetIcons(icons));
    }
  }

  @SubscribeEvent
  public void attachEntitiesCapabilities(AttachCapabilitiesEvent<Entity> evt) {

    if (evt.getObject() instanceof PlayerEntity) {
      evt.addCapability(CuriosCapability.ID_INVENTORY,
          CurioInventoryCapability.createProvider((PlayerEntity) evt.getObject()));
    }
  }

  /**
   * Handler for registering item's capabilities implemented through IItemCurio interface.
   */

  @SubscribeEvent
  public void attachStackCapabilities(AttachCapabilitiesEvent<ItemStack> evt) {
    ItemStack stack = evt.getObject();

    if (stack.getItem() instanceof ICurioItem) {
      ICurioItem itemCurio = (ICurioItem) stack.getItem();

      if (itemCurio.hasCurioCapability(stack)) {
        ItemizedCurioCapability itemizedCapability = new ItemizedCurioCapability(itemCurio, stack);
        evt.addCapability(CuriosCapability.ID_ITEM,
            CurioItemCapability.createProvider(itemizedCapability));
      }
    }
  }

  @SubscribeEvent
  public void entityJoinWorld(EntityJoinWorldEvent evt) {

    Entity entity = evt.getEntity();

    if (entity instanceof ServerPlayerEntity) {
      ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) entity;
      CuriosApi.getCuriosHelper().getCuriosHandler(serverPlayerEntity).ifPresent(handler -> {
        ServerPlayerEntity mp = (ServerPlayerEntity) entity;
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> mp),
            new SPacketSyncCurios(mp.getEntityId(), handler.getCurios()));
      });
    }
  }

  @SubscribeEvent
  public void playerStartTracking(PlayerEvent.StartTracking evt) {

    Entity target = evt.getTarget();
    PlayerEntity player = evt.getPlayer();

    if (player instanceof ServerPlayerEntity && target instanceof LivingEntity) {
      LivingEntity livingBase = (LivingEntity) target;
      CuriosApi.getCuriosHelper().getCuriosHandler(livingBase).ifPresent(
          handler -> NetworkHandler.INSTANCE
              .send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                  new SPacketSyncCurios(target.getEntityId(), handler.getCurios())));
    }
  }

  @SubscribeEvent
  public void playerClone(PlayerEvent.Clone evt) {
    PlayerEntity player = evt.getPlayer();

    PlayerEntity oldPlayer = evt.getOriginal();
    oldPlayer.revive();
    LazyOptional<ICuriosItemHandler> oldHandler = CuriosApi.getCuriosHelper()
        .getCuriosHandler(oldPlayer);
    LazyOptional<ICuriosItemHandler> newHandler = CuriosApi.getCuriosHelper()
        .getCuriosHandler(player);

    oldHandler.ifPresent(oldCurios -> newHandler.ifPresent(newCurios -> {
      newCurios.setCurios(new LinkedHashMap<>(oldCurios.getCurios()));

      oldCurios.getCurios().forEach((identifier, stacksHandler) -> {
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();

        for (int i = 0; i < stackHandler.getSlots(); i++) {
          ItemStack stack = stackHandler.getStackInSlot(i);
          SlotContext slotContext = new SlotContext(identifier, player, i);

          if (!stack.isEmpty()) {
            UUID uuid = UUID.nameUUIDFromBytes((identifier + i).getBytes());
            player.getAttributeManager().reapplyModifiers(
                CuriosApi.getCuriosHelper().getAttributeModifiers(slotContext, uuid, stack));
            CuriosApi.getCuriosHelper().getCurio(stack)
                .ifPresent(curio -> curio.onEquip(slotContext, ItemStack.EMPTY));

            if (player instanceof ServerPlayerEntity) {
              EquipCurioTrigger.INSTANCE
                  .trigger((ServerPlayerEntity) player, stack, (ServerWorld) player.world,
                      player.getPosX(), player.getPosY(), player.getPosZ());
            }
          }
        }
      });
    }));
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void playerDrops(LivingDropsEvent evt) {

    LivingEntity livingEntity = evt.getEntityLiving();

    if (!livingEntity.isSpectator()) {

      CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {
        Collection<ItemEntity> drops = evt.getDrops();
        Collection<ItemEntity> curioDrops = new ArrayList<>();
        Map<String, ICurioStacksHandler> curios = handler.getCurios();

        DropRulesEvent dropRulesEvent = new DropRulesEvent(livingEntity, handler, evt.getSource(),
            evt.getLootingLevel(), evt.isRecentlyHit());
        MinecraftForge.EVENT_BUS.post(dropRulesEvent);
        List<Tuple<Predicate<ItemStack>, DropRule>> dropRules = dropRulesEvent.getOverrides();

        boolean keepInventory = livingEntity.world.getGameRules()
            .getBoolean(GameRules.KEEP_INVENTORY);

        curios.forEach((id, stacksHandler) -> {
          handleDrops(livingEntity, dropRules, stacksHandler.getStacks(), curioDrops,
              keepInventory);
          handleDrops(livingEntity, dropRules, stacksHandler.getCosmeticStacks(), curioDrops,
              keepInventory);
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
    PlayerEntity player = evt.getPlayer();

    if (!player.world.isRemote) {
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
    PlayerEntity player = evt.getPlayer();
    ItemStack stack = evt.getItemStack();
    ICuriosHelper curiosHelper = CuriosApi.getCuriosHelper();
    curiosHelper.getCurio(stack).ifPresent(
        curio -> curiosHelper.getCuriosHandler(player).ifPresent(handler -> {

          if (!player.world.isRemote) {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();

            for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
              IDynamicStackHandler stackHandler = entry.getValue().getStacks();

              for (int i = 0; i < stackHandler.getSlots(); i++) {
                String id = entry.getKey();
                SlotContext slotContext = new SlotContext(id, player, i);

                if (curiosHelper.isStackValid(slotContext, stack) && curio.canEquip(id, player) &&
                    curio.canEquipFromUse(slotContext)) {
                  ItemStack present = stackHandler.getStackInSlot(i);

                  if (present.isEmpty()) {
                    stackHandler.setStackInSlot(i, stack.copy());
                    curio.onEquipFromUse(slotContext);

                    if (!player.isCreative()) {
                      int count = stack.getCount();
                      stack.shrink(count);
                    }
                    evt.setCancellationResult(ActionResultType.SUCCESS);
                    evt.setCanceled(true);
                    return;
                  }
                }
              }
            }
          } else {
            evt.setCancellationResult(ActionResultType.func_233537_a_(player.world.isRemote()));
            evt.setCanceled(true);
          }
        }));
  }

  @SubscribeEvent
  public void worldTick(TickEvent.WorldTickEvent evt) {

    if (evt.world instanceof ServerWorld && dirtyTags) {
      PlayerList list = ((ServerWorld) evt.world).getServer().getPlayerList();
      ICuriosHelper curiosHelper = CuriosApi.getCuriosHelper();

      for (ServerPlayerEntity player : list.getPlayers()) {
        curiosHelper.getCuriosHandler(player).ifPresent(handler -> {

          for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
            ICurioStacksHandler stacksHandler = entry.getValue();
            String id = entry.getKey();
            IDynamicStackHandler stacks = stacksHandler.getStacks();
            IDynamicStackHandler cosmeticStacks = stacksHandler.getCosmeticStacks();
            replaceInvalidStacks(curiosHelper, player, id, stacks);
            replaceInvalidStacks(curiosHelper, player, id, cosmeticStacks);
          }
        });
      }
      dirtyTags = false;
    }
  }

  private static void replaceInvalidStacks(ICuriosHelper curiosHelper, ServerPlayerEntity player,
                                           String id, IDynamicStackHandler stacks) {
    for (int i = 0; i < stacks.getSlots(); i++) {
      ItemStack stack = stacks.getStackInSlot(i);
      SlotContext slotContext = new SlotContext(id, player, i);

      if (!stack.isEmpty() && !curiosHelper.isStackValid(slotContext, stack)) {
        stacks.setStackInSlot(i, ItemStack.EMPTY);
        ItemHandlerHelper.giveItemToPlayer(player, stack);
      }
    }
  }

  @SubscribeEvent
  public void looting(LootingLevelEvent event) {
    if (event.getDamageSource() != null) {
      if (event.getDamageSource().getTrueSource() instanceof LivingEntity) {
        LivingEntity living = (LivingEntity) event.getDamageSource().getTrueSource();

        CuriosApi.getCuriosHelper().getCuriosHandler(living).ifPresent(
            handler -> event.setLootingLevel(event.getLootingLevel() + handler.getLootingBonus()));
      }
    }
  }

  @SubscribeEvent
  public void tick(LivingEvent.LivingUpdateEvent evt) {
    LivingEntity livingEntity = evt.getEntityLiving();

    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {
      handler.handleInvalidStacks();
      Map<String, ICurioStacksHandler> curios = handler.getCurios();
      int totalFortuneBonus = 0;
      int totalLootingBonus = 0;

      for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
        ICurioStacksHandler stacksHandler = entry.getValue();
        String identifier = entry.getKey();
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();
        IDynamicStackHandler cosmeticStackHandler = stacksHandler.getCosmeticStacks();

        for (int i = 0; i < stackHandler.getSlots(); i++) {
          SlotContext slotContext = new SlotContext(identifier, livingEntity, i);
          ItemStack stack = stackHandler.getStackInSlot(i);
          LazyOptional<ICurio> currentCurio = CuriosApi.getCuriosHelper().getCurio(stack);
          final int index = i;

          if (!stack.isEmpty()) {
            stack.inventoryTick(livingEntity.world, livingEntity, -1, false);
            currentCurio.ifPresent(curio -> curio.curioTick(identifier, index, livingEntity));

            if (livingEntity.world.isRemote) {
              currentCurio.ifPresent(curio -> curio.curioAnimate(identifier, index, livingEntity));
            }

            totalFortuneBonus += currentCurio
                .map(curio -> curio.getFortuneBonus(identifier, livingEntity, stack, index))
                .orElse(0);
            totalLootingBonus += currentCurio
                .map(curio -> curio.getLootingBonus(identifier, livingEntity, stack, index))
                .orElse(0);
          }

          if (!livingEntity.world.isRemote) {
            ItemStack prevStack = stackHandler.getPreviousStackInSlot(i);

            if (!ItemStack.areItemStacksEqual(stack, prevStack)) {
              LazyOptional<ICurio> prevCurio = CuriosApi.getCuriosHelper().getCurio(prevStack);
              syncCurios(livingEntity, stack, currentCurio, prevCurio, identifier, index,
                  HandlerType.EQUIPMENT);
              MinecraftForge.EVENT_BUS
                  .post(new CurioChangeEvent(livingEntity, identifier, i, prevStack, stack));
              UUID uuid = UUID.nameUUIDFromBytes((identifier + i).getBytes());

              if (!prevStack.isEmpty()) {
                livingEntity.getAttributeManager().removeModifiers(
                    CuriosApi.getCuriosHelper()
                        .getAttributeModifiers(slotContext, uuid, prevStack));
                prevCurio.ifPresent(curio -> curio.onUnequip(slotContext, stack));
              }

              if (!stack.isEmpty()) {
                livingEntity.getAttributeManager().reapplyModifiers(
                    CuriosApi.getCuriosHelper().getAttributeModifiers(slotContext, uuid, stack));
                currentCurio.ifPresent(curio -> curio.onEquip(slotContext, prevStack));

                if (livingEntity instanceof ServerPlayerEntity) {
                  EquipCurioTrigger.INSTANCE.trigger((ServerPlayerEntity) livingEntity, stack,
                      (ServerWorld) livingEntity.world, livingEntity.getPosX(),
                      livingEntity.getPosY(), livingEntity.getPosZ());
                }
              }
              stackHandler.setPreviousStackInSlot(i, stack.copy());
            }
            ItemStack cosmeticStack = cosmeticStackHandler.getStackInSlot(i);
            ItemStack prevCosmeticStack = cosmeticStackHandler.getPreviousStackInSlot(i);

            if (!ItemStack.areItemStacksEqual(cosmeticStack, prevCosmeticStack)) {
              syncCurios(livingEntity, cosmeticStack,
                  CuriosApi.getCuriosHelper().getCurio(cosmeticStack),
                  CuriosApi.getCuriosHelper().getCurio(prevCosmeticStack), identifier, index,
                  HandlerType.COSMETIC);
              cosmeticStackHandler.setPreviousStackInSlot(index, cosmeticStack.copy());
            }
          }
        }
      }
      handler.processSlots();
      handler.setEnchantmentBonuses(new Tuple<>(totalFortuneBonus, totalLootingBonus));
    });
  }

  private static void syncCurios(LivingEntity livingEntity, ItemStack stack,
                                 LazyOptional<ICurio> currentCurio, LazyOptional<ICurio> prevCurio,
                                 String identifier, int index, HandlerType type) {
    boolean syncable =
        currentCurio.map(curio -> curio.canSync(identifier, index, livingEntity)).orElse(false)
            || prevCurio.map(curio -> curio.canSync(identifier, index, livingEntity)).orElse(false);
    CompoundNBT syncTag =
        syncable ? currentCurio.map(curio -> {
          CompoundNBT tag = curio.writeSyncData();
          return tag != null ? tag : new CompoundNBT();
        }).orElse(new CompoundNBT()) : new CompoundNBT();
    NetworkHandler.INSTANCE
        .send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> livingEntity),
            new SPacketSyncStack(livingEntity.getEntityId(), identifier, index, stack, type,
                syncTag));
  }
}
