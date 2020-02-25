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
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.CuriosCapability;
import top.theillusivec4.curios.api.capability.ICurio;
import top.theillusivec4.curios.api.capability.ICurio.DropRule;
import top.theillusivec4.curios.api.capability.ICurioItemHandler;
import top.theillusivec4.curios.api.event.LivingCurioChangeEvent;
import top.theillusivec4.curios.api.event.LivingCurioDropRulesEvent;
import top.theillusivec4.curios.api.event.LivingCurioDropsEvent;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;
import top.theillusivec4.curios.common.capability.CapCurioInventory;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncContents;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncContentsWithTag;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncMap;

public class EventHandlerCurios {

  @SubscribeEvent
  public void onCapabilitiesEntity(AttachCapabilitiesEvent<Entity> evt) {

    if (evt.getObject() instanceof PlayerEntity) {
      evt.addCapability(CuriosCapability.ID_INVENTORY,
          CapCurioInventory.createProvider((PlayerEntity) evt.getObject()));
    }
  }

  @SubscribeEvent
  public void onEntityJoinWorld(EntityJoinWorldEvent evt) {

    Entity entity = evt.getEntity();

    if (entity instanceof LivingEntity) {
      LivingEntity livingBase = (LivingEntity) evt.getEntity();
      CuriosAPI.getCuriosHandler(livingBase).ifPresent(handler -> {
        handler.dropInvalidCache();

        if (entity instanceof ServerPlayerEntity) {
          ServerPlayerEntity mp = (ServerPlayerEntity) entity;
          NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> mp),
              new SPacketSyncMap(mp.getEntityId(), handler.getCurioMap()));
        }
      });
    }
  }

  @SubscribeEvent
  public void onPlayerStartTracking(PlayerEvent.StartTracking evt) {

    Entity target = evt.getTarget();
    PlayerEntity player = evt.getPlayer();

    if (player instanceof ServerPlayerEntity && target instanceof LivingEntity) {
      LivingEntity livingBase = (LivingEntity) target;
      CuriosAPI.getCuriosHandler(livingBase).ifPresent(handler -> NetworkHandler.INSTANCE
          .send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
              new SPacketSyncMap(target.getEntityId(), handler.getCurioMap())));
    }
  }

  @SubscribeEvent
  public void onPlayerClone(PlayerEvent.Clone evt) {
    PlayerEntity player = evt.getPlayer();

    PlayerEntity oldPlayer = evt.getOriginal();
    oldPlayer.revive();
    LazyOptional<ICurioItemHandler> oldHandler = CuriosAPI.getCuriosHandler(oldPlayer);
    LazyOptional<ICurioItemHandler> newHandler = CuriosAPI.getCuriosHandler(player);

    oldHandler.ifPresent(oldCurios -> newHandler.ifPresent(newCurios -> {
      newCurios.setCurioMap(new TreeMap<>(oldCurios.getCurioMap()));
      oldCurios.getCurioMap().forEach((identifier, stackHandler) -> {
        for (int i = 0; i < stackHandler.getSlots(); i++) {
          ItemStack stack = stackHandler.getStackInSlot(i);
          CuriosAPI.getCurio(stack).ifPresent(curio -> {
            player.getAttributes().applyAttributeModifiers(curio.getAttributeModifiers(identifier));
            curio.onEquipped(identifier, player);
          });
        }
      });
    }));
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onPlayerDrops(LivingDropsEvent evt) {

    LivingEntity livingEntity = evt.getEntityLiving();

    if (!livingEntity.isSpectator()) {
      CuriosAPI.getCuriosHandler(livingEntity).ifPresent(handler -> {
        Collection<ItemEntity> drops = evt.getDrops();
        Collection<ItemEntity> curioDrops = new ArrayList<>();
        SortedMap<String, CurioStackHandler> curioMap = handler.getCurioMap();
        LivingCurioDropRulesEvent dropRulesEvent = new LivingCurioDropRulesEvent(livingEntity,
            handler, evt.getSource(), evt.getLootingLevel(), evt.isRecentlyHit());
        MinecraftForge.EVENT_BUS.post(dropRulesEvent);
        List<Tuple<Predicate<ItemStack>, DropRule>> dropRules = dropRulesEvent.getOverrides();
        boolean keepInventory = livingEntity.world.getGameRules()
            .getBoolean(GameRules.KEEP_INVENTORY);

        for (String identifier : curioMap.keySet()) {
          ItemStackHandler stacks = curioMap.get(identifier);

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
                  : CuriosAPI.getCurio(stack).map(curio -> curio.getDropRule(livingEntity))
                      .orElse(DropRule.DEFAULT);

              if ((dropRule == DropRule.DEFAULT && keepInventory)
                  || dropRule == DropRule.ALWAYS_KEEP) {
                continue;
              }

              if (!EnchantmentHelper.hasVanishingCurse(stack) && dropRule != DropRule.DESTROY) {
                curioDrops.add(this.getDroppedItem(stack, livingEntity));
              }
              stacks.setStackInSlot(i, ItemStack.EMPTY);
            }
          }
        }

        if (!MinecraftForge.EVENT_BUS.post(
            new LivingCurioDropsEvent(livingEntity, handler, evt.getSource(), curioDrops,
                evt.getLootingLevel(), evt.isRecentlyHit()))) {
          drops.addAll(curioDrops);
        }
      });
    }
  }

  @SubscribeEvent
  public void onPlayerXPPickUp(PlayerXpEvent.PickupXp evt) {
    PlayerEntity player = evt.getPlayer();

    if (!player.world.isRemote) {
      CuriosAPI.getCuriosHandler(player).ifPresent(handler -> {
        SortedMap<String, CurioStackHandler> curioMap = handler.getCurioMap();

        for (String identifier : curioMap.keySet()) {
          ItemStackHandler stacks = curioMap.get(identifier);

          for (int i = 0; i < stacks.getSlots(); i++) {
            ItemStack stack = stacks.getStackInSlot(i);

            if (!stack.isEmpty()
                && EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack) > 0 && stack
                .isDamaged()) {
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
              return;
            }
          }
        }
      });
    }
  }

  private ItemEntity getDroppedItem(ItemStack droppedItem, LivingEntity livingEntity) {
    double d0 = livingEntity.getPosY() - 0.30000001192092896D + (double) livingEntity
        .getEyeHeight();
    ItemEntity entityitem = new ItemEntity(livingEntity.world, livingEntity.getPosX(), d0,
        livingEntity.getPosZ(), droppedItem);
    entityitem.setPickupDelay(40);
    float f = livingEntity.world.rand.nextFloat() * 0.5F;
    float f1 = livingEntity.world.rand.nextFloat() * ((float) Math.PI * 2F);
    entityitem.setMotion((-MathHelper.sin(f1) * f), 0.20000000298023224D, (MathHelper.cos(f1) * f));
    return entityitem;
  }

  @SubscribeEvent
  public void onCurioRightClick(PlayerInteractEvent.RightClickItem evt) {

    PlayerEntity player = evt.getPlayer();
    ItemStack stack = evt.getItemStack();
    CuriosAPI.getCurio(stack).ifPresent(curio -> {

      if (curio.canRightClickEquip()) {
        CuriosAPI.getCuriosHandler(player).ifPresent(handler -> {

          if (!player.world.isRemote) {
            SortedMap<String, CurioStackHandler> curios = handler.getCurioMap();
            Set<String> tags = CuriosAPI.getCurioTags(stack.getItem());

            for (String id : tags) {

              if (curio.canEquip(id, player)) {
                ItemStackHandler stackHandler = curios.get(id);

                if (stackHandler != null) {

                  for (int i = 0; i < stackHandler.getSlots(); i++) {

                    if (stackHandler.getStackInSlot(i).isEmpty()) {
                      stackHandler.setStackInSlot(i, stack.copy());
                      curio.playEquipSound(player);

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
  public void onCurioTick(LivingEvent.LivingUpdateEvent evt) {

    LivingEntity entitylivingbase = evt.getEntityLiving();
    CuriosAPI.getCuriosHandler(entitylivingbase).ifPresent(handler -> {
      SortedMap<String, CurioStackHandler> curios = handler.getCurioMap();

      for (String identifier : curios.keySet()) {
        CurioStackHandler stackHandler = curios.get(identifier);

        for (int i = 0; i < stackHandler.getSlots(); i++) {
          ItemStack stack = stackHandler.getStackInSlot(i);
          stack.inventoryTick(entitylivingbase.world, entitylivingbase, -1, false);
          LazyOptional<ICurio> currentCurio = CuriosAPI.getCurio(stack);
          final int index = i;
          currentCurio.ifPresent(curio -> curio.onCurioTick(identifier, index, entitylivingbase));

          if (!entitylivingbase.world.isRemote) {
            ItemStack prevStack = stackHandler.getPreviousStackInSlot(i);

            if (!ItemStack.areItemStacksEqual(stack, prevStack)) {
              LazyOptional<ICurio> prevCurio = CuriosAPI.getCurio(prevStack);
              boolean shouldSync = !stack.equals(prevStack, true);
              CompoundNBT syncTag = new CompoundNBT();
              boolean currentSyncFlag = currentCurio
                  .map(curio -> curio.shouldSyncToTracking(identifier, entitylivingbase))
                  .orElse(false);
              boolean prevSyncFlag = prevCurio
                  .map(curio -> curio.shouldSyncToTracking(identifier, entitylivingbase))
                  .orElse(false);

              if (currentSyncFlag || prevSyncFlag || shouldSync) {

                if (currentCurio.isPresent()) {
                  syncTag = currentCurio.map(ICurio::getSyncTag).orElse(syncTag);
                }

                if (!syncTag.isEmpty()) {
                  NetworkHandler.INSTANCE
                      .send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entitylivingbase),
                          new SPacketSyncContentsWithTag(entitylivingbase.getEntityId(), identifier,
                              i, stack, syncTag));
                } else {
                  NetworkHandler.INSTANCE
                      .send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entitylivingbase),
                          new SPacketSyncContents(entitylivingbase.getEntityId(), identifier, i,
                              stack));
                }
              }
              MinecraftForge.EVENT_BUS.post(
                  new LivingCurioChangeEvent(entitylivingbase, identifier, i, prevStack, stack));

              boolean changeEquipped = !ItemStack.areItemsEqualIgnoreDurability(prevStack, stack);
              prevCurio.ifPresent(curio -> {
                entitylivingbase.getAttributes()
                    .removeAttributeModifiers(curio.getAttributeModifiers(identifier));

                if (changeEquipped) {
                  curio.onUnequipped(identifier, entitylivingbase);
                }
              });
              currentCurio.ifPresent(curio -> {
                entitylivingbase.getAttributes()
                    .applyAttributeModifiers(curio.getAttributeModifiers(identifier));

                if (changeEquipped) {
                  curio.onEquipped(identifier, entitylivingbase);
                }
              });
              stackHandler
                  .setPreviousStackInSlot(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
          }
        }
      }
    });
  }
}
