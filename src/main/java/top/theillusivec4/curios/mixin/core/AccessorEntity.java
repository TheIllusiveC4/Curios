package top.theillusivec4.curios.mixin.core;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface AccessorEntity {

  @Accessor
  boolean getFirstTick();
}
