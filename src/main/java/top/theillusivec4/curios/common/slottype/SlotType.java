package top.theillusivec4.curios.common.slottype;

import java.util.Objects;
import net.minecraft.util.Identifier;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;

public final class SlotType implements ISlotType {

  private final String identifier;
  private final int priority;
  private final int size;
  private final boolean locked;
  private final boolean visible;
  private final boolean cosmetic;
  private final Identifier icon;

  private SlotType(Builder builder) {
    this.identifier = builder.identifier;
    this.priority = builder.priority != null ? builder.priority : 100;
    this.size = builder.size;
    this.locked = builder.locked;
    this.visible = builder.visible;
    this.cosmetic = builder.cosmetic;
    this.icon = builder.icon != null ? builder.icon
        : new Identifier(CuriosApi.MODID, "item/empty_curio_slot");
  }

  @Override
  public String getIdentifier() {
    return this.identifier;
  }

  @Override
  public Identifier getIcon() {
    return this.icon;
  }

  @Override
  public int getPriority() {
    return this.priority;
  }

  @Override
  public int getSize() {
    return this.size;
  }

  @Override
  public boolean isLocked() {
    return this.locked;
  }

  @Override
  public boolean isVisible() {
    return this.visible;
  }

  @Override
  public boolean hasCosmetic() {
    return this.cosmetic;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SlotType that = (SlotType) o;
    return identifier.equals(that.identifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifier);
  }

  @Override
  public int compareTo(ISlotType otherType) {

    if (this.priority == otherType.getPriority()) {
      return this.identifier.compareTo(otherType.getIdentifier());
    } else if (this.priority > otherType.getPriority()) {
      return 1;
    } else {
      return -1;
    }
  }

  public static class Builder {

    private final String identifier;
    private Integer priority;
    private int size = 1;
    private boolean locked = false;
    private boolean visible = true;
    private boolean cosmetic = false;
    private Identifier icon = null;

    public Builder(String identifier) {
      this.identifier = identifier;
    }

    public Builder copyFrom(Builder builder) {
      this.priority = builder.priority;
      this.size = builder.size;
      this.locked = builder.locked;
      this.visible = builder.visible;
      this.cosmetic = builder.cosmetic;
      this.icon = builder.icon;
      return this;
    }

    public Builder icon(Identifier icon) {
      this.icon = icon;
      return this;
    }

    public Builder priority(Integer priority) {
      return priority(priority, false);
    }

    public Builder priority(Integer priority, boolean force) {

      if (priority != null) {

        if (this.priority != null) {
          this.priority = force ? priority : Math.min(this.priority, priority);
        } else {
          this.priority = priority;
        }
      }
      return this;
    }

    public Builder size(int size) {
      return size(size, false);
    }

    public Builder size(int size, boolean force) {
      this.size = force ? size : Math.max(this.size, size);
      return this;
    }

    public Builder locked(boolean locked) {
      return locked(locked, false);
    }

    public Builder locked(boolean locked, boolean force) {
      this.locked = force ? locked : this.locked || locked;
      return this;
    }

    public Builder visible(boolean visible) {
      return visible(visible, false);
    }

    public Builder visible(boolean visible, boolean force) {
      this.visible = force ? visible : this.visible && visible;
      return this;
    }

    public Builder hasCosmetic(boolean cosmetic) {
      return hasCosmetic(cosmetic, false);
    }

    public Builder hasCosmetic(boolean cosmetic, boolean force) {
      this.cosmetic = force ? cosmetic : this.cosmetic || cosmetic;
      return this;
    }

    public SlotType build() {
      return new SlotType(this);
    }
  }
}
