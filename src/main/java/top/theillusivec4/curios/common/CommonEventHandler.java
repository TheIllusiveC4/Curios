package c4.curios.common;

import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.CapCurioInventory;
import c4.curios.api.capability.CapCurioItem;
import c4.curios.api.capability.ICurio;
import c4.curios.api.capability.ICurioItemHandler;
import com.google.common.collect.ImmutableMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class CommonEventHandler {

    @SubscribeEvent
    public void onCapabilitiesEntity(AttachCapabilitiesEvent<Entity> evt) {
        if (evt.getObject() instanceof EntityPlayer) {
            evt.addCapability(CapCurioInventory.ID, CapCurioInventory.createProvider(new CapCurioInventory.CurioInventoryWrapper()));
        }
    }

    @SubscribeEvent
    public void onCapabilitiesItemStack(AttachCapabilitiesEvent<ItemStack> evt) {
        ItemStack stack = evt.getObject();

        if (!stack.isEmpty() && stack.getItem() instanceof ICurio) {
            evt.addCapability(CapCurioItem.ID, CapCurioItem.createProvider((ICurio)stack.getItem()));
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone evt) {
        EntityPlayer player = evt.getEntityPlayer();
        if (!evt.isWasDeath() || player.world.getGameRules().getBoolean("keepInventory")) {
            ICurioItemHandler originalCurios = CuriosAPI.getCuriosHandler(evt.getOriginal());
            ICurioItemHandler newCurios = CuriosAPI.getCuriosHandler(player);

            if (originalCurios != null && newCurios != null) {
                newCurios.setCurioMap(originalCurios.getCurioMap());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDrops(PlayerDropsEvent evt) {
        EntityPlayer player = evt.getEntityPlayer();

        if (!player.world.getGameRules().getBoolean("keepInventory") && !player.isSpectator()) {
            ICurioItemHandler curios = CuriosAPI.getCuriosHandler(player);

            if (curios != null) {
                List<EntityItem> entityItems = evt.getDrops();
                ImmutableMap<String, ItemStackHandler> curioMap = curios.getCurioMap();

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
            }
        }
    }

    @SubscribeEvent
    public void onPlayerXPPickUp(PlayerPickupXpEvent evt) {
        EntityPlayer player = evt.getEntityPlayer();

        if (!player.world.isRemote) {
            ICurioItemHandler curios = CuriosAPI.getCuriosHandler(player);

            if (curios != null) {
                ImmutableMap<String, ItemStackHandler> curioMap = curios.getCurioMap();

                for (String identifier : curioMap.keySet()) {
                    ItemStackHandler stacks = curioMap.get(identifier);

                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);

                        if (!stack.isEmpty() && stack.isItemDamaged() && EnchantmentHelper.getEnchantmentLevel(
                                Enchantments.MENDING, stack) > 0) {
                            evt.setCanceled(true);
                            EntityXPOrb orb = evt.getOrb();
                            player.xpCooldown = 2;
                            player.onItemPickup(orb, 1);
                            int toRepair = Math.min(orb.xpValue * 2, stack.getItemDamage());
                            orb.xpValue -= toRepair / 2;
                            stack.setItemDamage(stack.getItemDamage() - toRepair);

                            if (orb.xpValue > 0) {
                                player.addExperience(orb.xpValue);
                            }
                            orb.setDead();
                            return;
                        }
                    }
                }
            }
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
