package top.theillusivec4.curios.api.internal.services.client;

import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@ApiStatus.Internal
public interface ICuriosClientExtensions {

  void registerCurioRenderer(Item item, Supplier<ICurioRenderer> curioRenderer);

  ICurioRenderer getCurioRenderer(Item item);
}
