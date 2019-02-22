/*
 * Copyright (C) 2018-2019  C4
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

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.CuriosRegistry;
import top.theillusivec4.curios.api.capability.CuriosCapability;
import top.theillusivec4.curios.api.capability.ICurio;
import top.theillusivec4.curios.api.event.LivingCurioChangeEvent;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;
import top.theillusivec4.curios.common.capability.CapCurioInventory;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncContents;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncMap;

import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;

public class EventHandlerCurios {

    @SubscribeEvent
    public void onCapabilitiesEntity(AttachCapabilitiesEvent<Entity> evt) {
        if (evt.getObject() instanceof EntityPlayer) {
            evt.addCapability(CuriosCapability.ID_INVENTORY, CapCurioInventory.createProvider((EntityPlayer)evt.getObject()));
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent evt) {
        Entity entity = evt.getEntity();

        if (entity instanceof EntityLivingBase) {
            EntityLivingBase livingBase = (EntityLivingBase)evt.getEntity();
            CuriosAPI.getCuriosHandler(livingBase).ifPresent(handler -> {
                handler.dropInvalidCache();

                if (entity instanceof EntityPlayerMP) {
                    EntityPlayerMP mp = (EntityPlayerMP)entity;
                    NetworkHandler.INSTANCE.sendTo(new SPacketSyncMap(mp.getEntityId(), handler.getCurioMap()),
                            mp.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerStartTracking(PlayerEvent.StartTracking evt) {
        Entity target = evt.getTarget();
        EntityPlayer player = evt.getEntityPlayer();

        if (player instanceof EntityPlayerMP && target instanceof EntityLivingBase) {
            EntityLivingBase livingBase = (EntityLivingBase)target;
            CuriosAPI.getCuriosHandler(livingBase).ifPresent(handler -> NetworkHandler.INSTANCE.sendTo(new SPacketSyncMap(livingBase.getEntityId(), handler.getCurioMap()),
                    ((EntityPlayerMP)player).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT));
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone evt) {
        EntityPlayer player = evt.getEntityPlayer();
        if (!evt.isWasDeath() || player.world.getGameRules().getBoolean("keepInventory")) {

            CuriosAPI.getCuriosHandler(evt.getOriginal()).ifPresent(originalHandler ->
                    CuriosAPI.getCuriosHandler(player).ifPresent(newHandler ->
                            newHandler.setCurioMap(originalHandler.getCurioMap())));
        }
    }

    @SubscribeEvent
    public void onPlayerDrops(PlayerDropsEvent evt) {
        EntityPlayer player = evt.getEntityPlayer();

        if (!player.world.getGameRules().getBoolean("keepInventory") && !player.isSpectator()) {
            CuriosAPI.getCuriosHandler(player).ifPresent(handler -> {
                Collection<EntityItem> entityItems = evt.getDrops();
                SortedMap<String, CurioStackHandler> curioMap = handler.getCurioMap();

                for (String identifier : curioMap.keySet()) {
                    ItemStackHandler stacks = curioMap.get(identifier);

                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);

                        if (!stack.isEmpty()) {
                            if (!EnchantmentHelper.hasVanishingCurse(stack)) {
                                entityItems.add(this.getDroppedItem(stack, player));
                            }
                            stacks.setStackInSlot(i, ItemStack.EMPTY);
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerXPPickUp(PlayerPickupXpEvent evt) {
        EntityPlayer player = evt.getEntityPlayer();

        if (!player.world.isRemote) {
            CuriosAPI.getCuriosHandler(player).ifPresent(handler -> {
                SortedMap<String, CurioStackHandler> curioMap = handler.getCurioMap();

                for (String identifier : curioMap.keySet()) {
                    ItemStackHandler stacks = curioMap.get(identifier);

                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);

                        if (!stack.isEmpty() && EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack) > 0
                                && stack.isDamaged()) {
                            evt.setCanceled(true);
                            EntityXPOrb orb = evt.getOrb();
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

    private EntityItem getDroppedItem(ItemStack droppedItem, EntityPlayer player) {
        double d0 = player.posY - 0.30000001192092896D + (double)player.getEyeHeight();
        EntityItem entityitem = new EntityItem(player.world, player.posX, d0, player.posZ, droppedItem);
        entityitem.setPickupDelay(40);
        float f = player.world.rand.nextFloat() * 0.5F;
        float f1 = player.world.rand.nextFloat() * ((float)Math.PI * 2F);
        entityitem.motionX = (double)(-MathHelper.sin(f1) * f);
        entityitem.motionZ = (double)(MathHelper.cos(f1) * f);
        entityitem.motionY = 0.20000000298023224D;
        return entityitem;
    }

    @SubscribeEvent
    public void onCurioRightClick(PlayerInteractEvent.RightClickItem evt) {
        EntityPlayer player = evt.getEntityPlayer();
        ItemStack stack = evt.getItemStack();
        CuriosAPI.getCurio(stack).ifPresent(curio -> {

            if (curio.canRightClickEquip(stack)) {
                CuriosAPI.getCuriosHandler(player).ifPresent(handler -> {

                    if (!player.world.isRemote) {
                        SortedMap<String, CurioStackHandler> curios = handler.getCurioMap();
                        Set<String> tags = CuriosRegistry.getCurioTags(stack.getItem());

                        for (String id : tags) {

                            if (curio.canEquip(stack, id, player)) {
                                ItemStackHandler stackHandler = curios.get(id);

                                if (stackHandler != null) {

                                    for (int i = 0; i < stackHandler.getSlots(); i++) {

                                        if (stackHandler.getStackInSlot(i).isEmpty()) {
                                            stackHandler.setStackInSlot(i, stack.copy());
                                            curio.onEquipped(stack, id, player);
                                            stack.shrink(1);
                                            evt.setCancellationResult(EnumActionResult.SUCCESS);
                                            evt.setCanceled(true);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            } else {
                evt.setCancellationResult(EnumActionResult.FAIL);
                evt.setCanceled(true);
            }
        });
    }

    @SubscribeEvent
    public void onCurioTick(LivingEvent.LivingUpdateEvent evt) {
        EntityLivingBase entitylivingbase = evt.getEntityLiving();
        CuriosAPI.getCuriosHandler(entitylivingbase).ifPresent(handler -> {
            SortedMap<String, CurioStackHandler> curios = handler.getCurioMap();

            for (String identifier : curios.keySet()) {
                CurioStackHandler stackHandler = curios.get(identifier);

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);
                    stack.inventoryTick(entitylivingbase.world, entitylivingbase, -1, false);
                    LazyOptional<ICurio> currentCurio = CuriosAPI.getCurio(stack);
                    currentCurio.ifPresent(curio -> curio.onCurioTick(stack, identifier, entitylivingbase));

                    if (!entitylivingbase.world.isRemote) {
                        ItemStack prevStack = stackHandler.getPreviousStackInSlot(i);

                        if (!stack.equals(prevStack, true)) {

                            if (currentCurio.map(curio -> curio.shouldSyncToTracking(stack, prevStack, identifier, entitylivingbase))
                                    .orElse(stack.getItem() != prevStack.getItem())) {
                                EntityTracker tracker = ((WorldServer) entitylivingbase.world).getEntityTracker();

                                for (EntityPlayer player : tracker.getTrackingPlayers(entitylivingbase)) {

                                    if (player instanceof EntityPlayerMP) {
                                        NetworkHandler.INSTANCE.sendTo(new SPacketSyncContents(entitylivingbase.getEntityId(), identifier, i, stack),
                                                ((EntityPlayerMP) player).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
                                    }
                                }
                            }
                            MinecraftForge.EVENT_BUS.post(new LivingCurioChangeEvent(entitylivingbase, identifier, i, prevStack, stack));
                            CuriosAPI.getCurio(prevStack).ifPresent(curio -> entitylivingbase.getAttributeMap().removeAttributeModifiers(curio.getAttributeModifiers(identifier, prevStack)));
                            currentCurio.ifPresent(curio -> entitylivingbase.getAttributeMap().applyAttributeModifiers(curio.getAttributeModifiers(identifier, stack)));
                            stackHandler.setPreviousStackInSlot(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());

                            if (entitylivingbase instanceof EntityPlayerMP) {
                                NetworkHandler.INSTANCE.sendTo(new SPacketSyncContents(entitylivingbase.getEntityId(), identifier, i, stack),
                                        ((EntityPlayerMP) entitylivingbase).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);

                            }
                        }
                    }
                }
            }
        });
    }
}
