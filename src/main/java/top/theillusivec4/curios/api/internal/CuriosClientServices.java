package top.theillusivec4.curios.api.internal;

import org.jetbrains.annotations.ApiStatus;
import top.theillusivec4.curios.api.internal.services.client.ICuriosClientExtensions;

@ApiStatus.Internal
public class CuriosClientServices {

  public static final ICuriosClientExtensions EXTENSIONS =
      CuriosServices.load(ICuriosClientExtensions.class);
}
