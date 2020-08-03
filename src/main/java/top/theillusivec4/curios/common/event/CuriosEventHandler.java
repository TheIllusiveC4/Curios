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
import java.util.Set;
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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent.PickupXp;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.event.CurioDropsEvent;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurio.DropRule;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.capability.CurioInventoryCapability;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.SPacketSetIcons;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncCurios;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack.HandlerType;

public class CuriosEventHandler {

  private static void handleDrops(LivingEntity livingEntity,
      List<Tuple<Predicate<ItemStack>, DropRule>> dropRules, IDynamicStackHandler stacks,
      Collection<ItemEntity> drops, boolean keepInventory) {
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
    double d0 =
        livingEntity.getPosY() - 0.30000001192092896D + (double) livingEntity.getEyeHeight();
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
          player.func_233645_dx_()
              .func_233793_b_(CuriosApi.getCuriosHelper().getAttributeModifiers(identifier, stack));
          int index = i;
          CuriosApi.getCuriosHelper().getCurio(stack)
              .ifPresent(curio -> curio.onEquip(identifier, index, player));
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
    CuriosApi.getCuriosHelper().getCurio(stack).ifPresent(curio -> {

      if (curio.canRightClickEquip()) {
        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {

          if (!player.world.isRemote) {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();

            for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
              IDynamicStackHandler stackHandler = entry.getValue().getStacks();

              for (int i = 0; i < stackHandler.getSlots(); i++) {
                ItemStack present = stackHandler.getStackInSlot(i);
                Set<String> tags = CuriosApi.getCuriosHelper().getCurioTags(stack.getItem());
                String id = entry.getKey();

                if (present.isEmpty() && (tags.contains(id) || tags.contains("curio")) && curio
                    .canEquip(id, player)) {
                  stackHandler.setStackInSlot(i, stack.copy());
                  curio.playRightClickEquipSound(player);

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
          } else {
            evt.setCancellationResult(ActionResultType.SUCCESS);
            evt.setCanceled(true);
          }
        });
      }
    });
  }

  @SubscribeEvent
  public void tick(LivingEvent.LivingUpdateEvent evt) {
    LivingEntity livingEntity = evt.getEntityLiving();
    CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).ifPresent(handler -> {
      handler.handleInvalidStacks();
      Map<String, ICurioStacksHandler> curios = handler.getCurios();

      for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
        ICurioStacksHandler stacksHandler = entry.getValue();
        String identifier = entry.getKey();
        IDynamicStackHandler stackHandler = stacksHandler.getStacks();
        IDynamicStackHandler cosmeticStackHandler = stacksHandler.getCosmeticStacks();

        for (int i = 0; i < stackHandler.getSlots(); i++) {
          ItemStack stack = stackHandler.getStackInSlot(i);
          LazyOptional<ICurio> currentCurio = CuriosApi.getCuriosHelper().getCurio(stack);
          final int index = i;

          if (!stack.isEmpty()) {
            stack.inventoryTick(livingEntity.world, livingEntity, -1, false);
            currentCurio.ifPresent(curio -> {
              curio.curioTick(identifier, index, livingEntity);

              if (livingEntity.world.isRemote) {
                curio.curioAnimate(identifier, index, livingEntity);
              }
            });
          }

          if (!livingEntity.world.isRemote) {
            ItemStack prevStack = stackHandler.getPreviousStackInSlot(i);

            if (!ItemStack.areItemStacksEqual(stack, prevStack)) {
              LazyOptional<ICurio> prevCurio = CuriosApi.getCuriosHelper().getCurio(prevStack);
              syncCurios(livingEntity, stack, currentCurio, prevCurio, identifier, index,
                  HandlerType.EQUIPMENT);
              MinecraftForge.EVENT_BUS
                  .post(new CurioChangeEvent(livingEntity, identifier, i, prevStack, stack));
              livingEntity.func_233645_dx_().func_233785_a_(
                  CuriosApi.getCuriosHelper().getAttributeModifiers(identifier, prevStack));
              livingEntity.func_233645_dx_().func_233793_b_(
                  CuriosApi.getCuriosHelper().getAttributeModifiers(identifier, stack));
              prevCurio.ifPresent(curio -> curio.onUnequip(identifier, index, livingEntity));
              currentCurio.ifPresent(curio -> curio.onEquip(identifier, index, livingEntity));
              stackHandler
                  .setPreviousStackInSlot(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
            ItemStack cosmeticStack = cosmeticStackHandler.getStackInSlot(i);
            ItemStack prevCosmeticStack = cosmeticStackHandler.getPreviousStackInSlot(i);

            if (!ItemStack.areItemStacksEqual(cosmeticStack, prevCosmeticStack)) {
              syncCurios(livingEntity, cosmeticStack,
                  CuriosApi.getCuriosHelper().getCurio(cosmeticStack),
                  CuriosApi.getCuriosHelper().getCurio(prevCosmeticStack), identifier, index,
                  HandlerType.COSMETIC);
              cosmeticStackHandler.setPreviousStackInSlot(index,
                  cosmeticStack.isEmpty() ? ItemStack.EMPTY : cosmeticStack.copy());
            }
          }
        }
      }
    });
  }

  private static void syncCurios(LivingEntity livingEntity, ItemStack stack,
      LazyOptional<ICurio> currentCurio, LazyOptional<ICurio> prevCurio, String identifier,
      int index, HandlerType type) {
    boolean syncable =
        currentCurio.map(curio -> curio.canSync(identifier, index, livingEntity)).orElse(false)
            || prevCurio.map(curio -> curio.canSync(identifier, index, livingEntity)).orElse(false);
    CompoundNBT syncTag =
        syncable ? currentCurio.map(ICurio::writeSyncData).orElse(new CompoundNBT())
            : new CompoundNBT();
    NetworkHandler.INSTANCE
        .send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> livingEntity),
            new SPacketSyncStack(livingEntity.getEntityId(), identifier, index, stack, type,
                syncTag));
  }
}
