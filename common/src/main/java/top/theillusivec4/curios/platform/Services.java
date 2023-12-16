package top.theillusivec4.curios.platform;

import java.util.ServiceLoader;
import top.theillusivec4.curios.CuriosConstants;
import top.theillusivec4.curios.platform.services.ICuriosPlatform;

public class Services {

  public static final ICuriosPlatform CURIOS = load(ICuriosPlatform.class);

  public static <T> T load(Class<T> clazz) {

    final T loadedService = ServiceLoader.load(clazz)
        .findFirst()
        .orElseThrow(
            () -> new NullPointerException("Failed to load service for " + clazz.getName()));
    CuriosConstants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
    return loadedService;
  }
}
