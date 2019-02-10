package c4.curios.common;

import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurio;
import c4.curios.api.capability.ICurioItemHandler;
import c4.curios.api.event.LivingChangeCurioEvent;
import c4.curios.api.inventory.CurioStackHandler;
import c4.curios.common.network.NetworkHandler;
import c4.curios.common.network.server.SPacketEntityCurios;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.Map;

public class CurioEventHandler {

    @SubscribeEvent
    public void onCurioTick(LivingEvent.LivingUpdateEvent evt) {
        EntityLivingBase entitylivingbase = evt.getEntityLiving();
        ICurioItemHandler curioHandler = CuriosAPI.getCuriosHandler(entitylivingbase);

        if (curioHandler != null) {

            if (!entitylivingbase.world.isRemote) {
                Map<String, CurioStackHandler> curios = curioHandler.getCurioMap();
                Map<String, CurioStackHandler> prevCurios = curioHandler.getPreviousCurioMap();

                for (String identifier : curios.keySet()) {
                    CurioStackHandler stackHandler = curios.get(identifier);
                    CurioStackHandler prevStackHandler = prevCurios.get(identifier);

                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack stack = stackHandler.getStackInSlot(i);
                        ItemStack prevStack = prevStackHandler.getStackInSlot(i);

                        ICurio curio = CuriosAPI.getCurio(stack);

                        if (curio != null) {
                            curio.onCurioTick(stack, entitylivingbase);
                        }

                        if (!ItemStack.areItemStacksEqual(stack, prevStack)) {

                            if (!ItemStack.areItemStacksEqualUsingNBTShareTag(stack, prevStack)
                                    && entitylivingbase.world instanceof WorldServer) {
                                EntityTracker tracker = ((WorldServer) entitylivingbase.world).getEntityTracker();

                                for (EntityPlayer player : tracker.getTrackingPlayers(entitylivingbase)) {

                                    if (player instanceof EntityPlayerMP) {
                                        NetworkHandler.INSTANCE.sendTo(new SPacketEntityCurios(entitylivingbase.getEntityId(),
                                                identifier, i, stack), (EntityPlayerMP)player);
                                    }
                                }
                            }
                            MinecraftForge.EVENT_BUS.post(new LivingChangeCurioEvent(entitylivingbase, identifier, prevStack, stack));
                            ICurio prevCurio = CuriosAPI.getCurio(prevStack);

                            if (!prevStack.isEmpty() && prevCurio != null) {
                                prevCurio.onUnequipped(prevStack, entitylivingbase);
                                entitylivingbase.getAttributeMap().removeAttributeModifiers(prevCurio.getAttributeModifiers(identifier, prevStack));
                            }

                            if (!stack.isEmpty() && curio != null) {
                                curio.onEquipped(stack, entitylivingbase);
                                entitylivingbase.getAttributeMap().applyAttributeModifiers(curio.getAttributeModifiers(identifier, stack));
                            }
                            prevStackHandler.setStackInSlot(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
                        }
                    }
                }
            }
        }
    }
}
