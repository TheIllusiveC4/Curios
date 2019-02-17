package top.theillusivec4.curios.common;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.ICurio;
import top.theillusivec4.curios.api.event.LivingCurioChangeEvent;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.SPacketEntityCurios;

public class CurioEventHandler {

    @SubscribeEvent
    public void onCurioTick(LivingEvent.LivingUpdateEvent evt) {
        EntityLivingBase entitylivingbase = evt.getEntityLiving();
        CuriosAPI.getCuriosHandler(entitylivingbase).ifPresent(handler -> {

            if (!entitylivingbase.world.isRemote) {
                ImmutableMap<String, ItemStackHandler> curios = handler.getCurioMap();
                ImmutableMap<String, ItemStackHandler> prevCurios = handler.getPreviousCurioMap();

                for (String identifier : curios.keySet()) {
                    ItemStackHandler stackHandler = curios.get(identifier);
                    ItemStackHandler prevStackHandler = prevCurios.get(identifier);

                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack stack = stackHandler.getStackInSlot(i);
                        ItemStack prevStack = prevStackHandler.getStackInSlot(i);

                        LazyOptional<ICurio> currentCurio = CuriosAPI.getCurio(stack);
                        currentCurio.ifPresent(curio -> curio.onCurioTick(stack, entitylivingbase));

                        if (!ItemStack.areItemStacksEqual(stack, prevStack)) {

                            if (!stack.equals(prevStack, true) && entitylivingbase.world instanceof WorldServer) {
                                EntityTracker tracker = ((WorldServer) entitylivingbase.world).getEntityTracker();

                                for (EntityPlayer player : tracker.getTrackingPlayers(entitylivingbase)) {

                                    if (player instanceof EntityPlayerMP) {
                                        NetworkHandler.INSTANCE.sendTo(
                                                new SPacketEntityCurios(entitylivingbase.getEntityId(), identifier, i, stack),
                                                ((EntityPlayerMP)player).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
                                    }
                                }
                            }
                            MinecraftForge.EVENT_BUS.post(new LivingCurioChangeEvent(entitylivingbase, identifier, prevStack, stack));
                            CuriosAPI.getCurio(prevStack).ifPresent(curio -> {
                                curio.onUnequipped(prevStack, entitylivingbase);
                                entitylivingbase.getAttributeMap().removeAttributeModifiers(curio.getAttributeModifiers(identifier, prevStack));
                            });
                            currentCurio.ifPresent(curio -> {
                                curio.onEquipped(stack, entitylivingbase);
                                entitylivingbase.getAttributeMap().applyAttributeModifiers(curio.getAttributeModifiers(identifier, stack));
                            });
                            prevStackHandler.setStackInSlot(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
                        }
                    }
                }
            }
        });
    }
}
