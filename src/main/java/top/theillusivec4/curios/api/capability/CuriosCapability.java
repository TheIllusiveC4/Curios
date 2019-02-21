package top.theillusivec4.curios.api.capability;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import top.theillusivec4.curios.Curios;

public class CuriosCapability {

    @CapabilityInject(ICurioItemHandler.class)
    public static final Capability<ICurioItemHandler> INVENTORY = null;

    @CapabilityInject(ICurio.class)
    public static final Capability<ICurio> ITEM = null;

    public static final ResourceLocation ID_INVENTORY = new ResourceLocation(Curios.MODID, "inventory");
    public static final ResourceLocation ID_ITEM = new ResourceLocation(Curios.MODID, "item");
}
