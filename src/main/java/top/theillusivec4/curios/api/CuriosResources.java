package top.theillusivec4.curios.api;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CuriosResources {

  public static final String MOD_ID = "curios";
  public static final String MOD_NAME = "Curios API";
  public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

  public static ResourceLocation resource(String path) {
    return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
  }
}
