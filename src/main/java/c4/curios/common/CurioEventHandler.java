package c4.curios.common;

import c4.curios.api.CuriosAPI;
import c4.curios.api.capability.ICurio;
import c4.curios.api.capability.ICurioItemHandler;
import c4.curios.api.event.LivingChangeCurioEvent;
import c4.curios.api.inventory.CurioSlot;
import c4.curios.common.network.NetworkHandler;
import c4.curios.common.network.server.SPacketEntityCurios;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CurioEventHandler {

    @SubscribeEvent
    public void onCurioTick(LivingEvent.LivingUpdateEvent evt) {
        EntityLivingBase entitiylivingbase = evt.getEntityLiving();
        ICurioItemHandler curioHandler = CuriosAPI.getCuriosHandler(entitiylivingbase);

        if (curioHandler != null) {

            if (!entitiylivingbase.world.isRemote) {
                NonNullList<CurioSlot> curios = curioHandler.getCurioStacks();
                NonNullList<CurioSlot> prevCurios = curioHandler.getPreviousCurioStacks();

                for (int i = 0; i < curios.size(); i++) {
                    ItemStack stack = curioHandler.getStackInSlot(i);
                    ItemStack prevStack = prevCurios.get(i).getStack();

                    if (!ItemStack.areItemStacksEqual(stack, prevStack)) {

                        if (!ItemStack.areItemStacksEqualUsingNBTShareTag(stack, prevStack)
                                && entitiylivingbase.world instanceof WorldServer) {
                            EntityTracker tracker = ((WorldServer) entitiylivingbase.world).getEntityTracker();

                            for (EntityPlayer player : tracker.getTrackingPlayers(entitiylivingbase)) {

                                if (player instanceof EntityPlayerMP) {
                                    NetworkHandler.INSTANCE.sendTo(new SPacketEntityCurios(entitiylivingbase.getEntityId(),
                                                    i, stack), (EntityPlayerMP)player);
                                }
                            }
                        }

                        String identifier = curios.get(i).getInfo().getIdentifier();
                        MinecraftForge.EVENT_BUS.post(new LivingChangeCurioEvent(entitiylivingbase, identifier, prevStack, stack));
                        ICurio prevCurio = CuriosAPI.getCurio(prevStack);
                        ICurio curio = CuriosAPI.getCurio(stack);

                        if (!prevStack.isEmpty() && prevCurio != null) {
                            prevCurio.onUnequipped(prevStack, entitiylivingbase);
                            entitiylivingbase.getAttributeMap().removeAttributeModifiers(prevCurio.getAttributeModifiers(prevStack));
                        }

                        if (!stack.isEmpty() && curio != null) {
                            curio.onEquipped(stack, entitiylivingbase);
                            entitiylivingbase.getAttributeMap().applyAttributeModifiers(curio.getAttributeModifiers(stack));
                        }
                    }
                }
            }
        }
    }
}
