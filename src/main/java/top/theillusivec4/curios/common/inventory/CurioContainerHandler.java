package top.theillusivec4.curios.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IInteractionObject;
import top.theillusivec4.curios.Curios;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CurioContainerHandler implements IInteractionObject {

    public static final ResourceLocation ID = new ResourceLocation(Curios.MODID, "container");

    @Nonnull
    @Override
    public Container createContainer(@Nonnull InventoryPlayer inventoryPlayer, @Nonnull EntityPlayer entityPlayer) {
        return new ContainerCurios(inventoryPlayer, entityPlayer);
    }

    @Nonnull
    @Override
    public String getGuiID() {
        return ID.toString();
    }

    @Nonnull
    @Override
    public ITextComponent getName() {
        return new TextComponentString(getGuiID());
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return null;
    }
}
