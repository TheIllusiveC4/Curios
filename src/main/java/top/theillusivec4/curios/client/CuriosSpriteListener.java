package top.theillusivec4.curios.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosSpriteListener extends SimplePreparableReloadListener<Void> {

  private final Set<ResourceLocation> registeredSprites = new HashSet<>();

  public Set<ResourceLocation> getSprites() {
    return registeredSprites;
  }

  @Nonnull
  @Override
  protected Void prepare(ResourceManager resourceManagerIn, @Nonnull ProfilerFiller profilerIn) {
    Collection<ResourceLocation> resources = resourceManagerIn.listResources("textures/slot",
        (resourceLocation) -> resourceLocation.getPath().endsWith(".png")).keySet();
    Set<ResourceLocation> result = new HashSet<>();

    for (ResourceLocation resource : resources) {
      String prefix = "textures/slot/";
      String namespace = resource.getNamespace();
      String path = resource.getPath();

      if (namespace.equals(CuriosApi.MODID) && path.startsWith(prefix)) {
        result.add(new ResourceLocation(namespace,
            path.substring("textures/".length(), path.length() - ".png".length())));
      }
    }
    registeredSprites.clear();
    registeredSprites.addAll(result);
    return null;
  }

  @Override
  protected void apply(@Nonnull Void p_10793_, @Nonnull ResourceManager p_10794_,
                       @Nonnull ProfilerFiller p_10795_) {
    // NO-OP
  }
}
