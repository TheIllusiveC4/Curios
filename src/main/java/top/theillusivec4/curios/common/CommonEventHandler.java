package top.theillusivec4.curios.common;

import com.google.common.collect.ImmutableMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.CapCurioInventory;

import java.util.Collection;

public class CommonEventHandler {

    @SubscribeEvent
    public void onCapabilitiesEntity(AttachCapabilitiesEvent<Entity> evt) {
        if (evt.getObject() instanceof EntityPlayer) {
            evt.addCapability(CapCurioInventory.ID, CapCurioInventory.createProvider((EntityPlayer)evt.getObject()));
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
                ImmutableMap<String, ItemStackHandler> curioMap = handler.getCurioMap();

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
                ImmutableMap<String, ItemStackHandler> curioMap = handler.getCurioMap();

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
}
