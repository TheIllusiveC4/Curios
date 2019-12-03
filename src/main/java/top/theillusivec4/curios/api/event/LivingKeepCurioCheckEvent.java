package top.theillusivec4.curios.api.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingEvent;
import top.theillusivec4.curios.api.capability.ICurioItemHandler;

public class LivingKeepCurioCheckEvent extends LivingEvent {

  private final DamageSource source;
  private final int lootingLevel;
  private final boolean recentlyHit;
  private final ICurioItemHandler curioHandler;
  private final Collection<BiPredicate<LivingKeepCurioCheckEvent, ItemStack>> keepPredicates = new ArrayList<>();

  public LivingKeepCurioCheckEvent(LivingEntity entity, ICurioItemHandler handler,
      DamageSource source, int lootingLevel, boolean recentlyHit) {
    super(entity);
    this.source = source;
    this.lootingLevel = lootingLevel;
    this.recentlyHit = recentlyHit;
    this.curioHandler = handler;
  }

  public DamageSource getSource() {
    return source;
  }

  public int getLootingLevel() {
    return lootingLevel;
  }

  public boolean isRecentlyHit() {
    return recentlyHit;
  }

  public ICurioItemHandler getCurioHandler() {
    return curioHandler;
  }

  public Collection<BiPredicate<LivingKeepCurioCheckEvent, ItemStack>> getKeepPredicates() {
    return keepPredicates;
  }
}
