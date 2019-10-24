package top.theillusivec4.curios.api.event;

import java.util.Collection;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import top.theillusivec4.curios.api.capability.ICurioItemHandler;

/**
 * LivingCurioDropsEvent is fired when an Entity's death causes dropped curios to appear.<br> This
 * event is fired whenever an Entity dies and drops items in {@link LivingEntity#onDeath(DamageSource)}.<br>
 * <br>
 * This event is fired inside the {@link net.minecraftforge.event.entity.living.LivingDropsEvent}.<br>
 * <br>
 * {@link #source} contains the DamageSource that caused the drop to occur.<br> {@link #drops}
 * contains the ArrayList of ItemEntity that will be dropped.<br> {@link #lootingLevel} contains the
 * amount of loot that will be dropped.<br> {@link #recentlyHit} determines whether the Entity doing
 * the drop has recently been damaged.<br>
 * <br>
 * This event is {@link Cancelable}.<br> If this event is canceled, the Entity does not drop
 * anything.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 **/
@net.minecraftforge.eventbus.api.Cancelable
public class LivingCurioDropsEvent extends LivingEvent {

  private final DamageSource source;
  private final Collection<ItemEntity> drops;
  private final int lootingLevel;
  private final boolean recentlyHit;
  private final ICurioItemHandler curioHandler;

  public LivingCurioDropsEvent(LivingEntity entity, ICurioItemHandler handler, DamageSource source,
      Collection<ItemEntity> drops, int lootingLevel, boolean recentlyHit) {
    super(entity);
    this.source = source;
    this.drops = drops;
    this.lootingLevel = lootingLevel;
    this.recentlyHit = recentlyHit;
    this.curioHandler = handler;
  }

  public ICurioItemHandler getCurioHandler() {
    return curioHandler;
  }

  public DamageSource getSource() {
    return source;
  }

  public Collection<ItemEntity> getDrops() {
    return drops;
  }

  public int getLootingLevel() {
    return lootingLevel;
  }

  public boolean isRecentlyHit() {
    return recentlyHit;
  }
}
