package c4.curios.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface IProxy {

    default void preInit(FMLPreInitializationEvent evt){}

    default void init(FMLInitializationEvent evt){}

    default void postInit(FMLPostInitializationEvent evt){}
}
