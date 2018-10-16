package c4.curios.client;

import c4.curios.common.inventory.ContainerCurios;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

    public static final int GUI_CURIO_ID = 0;

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

        if (ID == GUI_CURIO_ID) {
            return new ContainerCurios(player.inventory, player);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

        if (ID == GUI_CURIO_ID) {
            return new GuiContainerCurios(new ContainerCurios(player.inventory, player));
        }
        return null;
    }
}
