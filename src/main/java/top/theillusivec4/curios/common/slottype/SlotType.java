/*
 * Copyright (c) 2018-2020 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.curios.common.slottype;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.EnumUtils;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurio;

public final class SlotType implements ISlotType {

  private final String identifier;
  private final int order;
  private final int size;
  private final boolean useNativeGui;
  private final boolean hasCosmetic;
  private final ResourceLocation icon;
  private final ICurio.DropRule dropRule;
  private final boolean renderToggle;

  private SlotType(Builder builder) {
    this.identifier = builder.identifier;
    this.order = builder.order;
    this.size = builder.size;
    this.useNativeGui = builder.useNativeGui;
    this.hasCosmetic = builder.hasCosmetic;
    this.icon = builder.icon;
    this.dropRule = builder.dropRule;
    this.renderToggle = builder.renderToggle;
  }

  @Override
  public String getIdentifier() {
    return this.identifier;
  }

  @Override
  public ResourceLocation getIcon() {
    return this.icon;
  }

  @Override
  public int getOrder() {
    return this.order;
  }

  @Override
  public int getSize() {
    return this.size;
  }

  @Override
  public boolean useNativeGui() {
    return this.useNativeGui;
  }

  @Override
  public boolean hasCosmetic() {
    return this.hasCosmetic;
  }

  @Override
  public boolean canToggleRendering() {
    return this.renderToggle;
  }

  @Override
  public ICurio.DropRule getDropRule() {
    return this.dropRule;
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
    return this.identifier.equals(that.identifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.identifier);
  }

  @Override
  public int compareTo(ISlotType otherType) {

    if (this.order == otherType.getOrder()) {
      return this.identifier.compareTo(otherType.getIdentifier());
    } else if (this.order > otherType.getOrder()) {
      return 1;
    } else {
      return -1;
    }
  }

  public static class Builder {

    private final String identifier;
    private Integer order = null;
    private Integer size = null;
    private int sizeMod = 0;
    private Boolean useNativeGui = null;
    private Boolean hasCosmetic = null;
    private Boolean renderToggle = null;
    private ResourceLocation icon = new ResourceLocation(CuriosApi.MODID, "slot/empty_curio_slot");
    private ICurio.DropRule dropRule = ICurio.DropRule.DEFAULT;

    public Builder(String identifier) {
      this.identifier = identifier;
    }

    public void apply(Builder builder) {

      if (!builder.identifier.equals(this.identifier)) {
        Curios.LOGGER.error("Mismatched slot builders {} and {}", builder.identifier,
            this.identifier);
        return;
      }

      if (builder.order != null) {
        this.order(builder.order);
      }

      if (builder.size != null) {
        this.size(builder.size);
      }

      if (builder.useNativeGui != null) {
        this.useNativeGui(builder.useNativeGui);
      }

      if (builder.hasCosmetic != null) {
        this.hasCosmetic(builder.hasCosmetic);
      }

      if (builder.renderToggle != null) {
        this.renderToggle(builder.renderToggle);
      }

      if (builder.icon != null) {
        this.icon(builder.icon);
      }

      if (builder.dropRule != null) {
        this.dropRule(builder.dropRule);
      }
    }

    public Builder icon(ResourceLocation icon) {
      this.icon = icon;
      return this;
    }

    public Builder order(int order) {
      return order(order, false);
    }

    public Builder order(int order, boolean replace) {
      this.order = replace || this.order == null ? order : Math.min(this.order, order);
      return this;
    }

    public Builder size(int size) {
      return size(size, false);
    }

    public Builder size(int size, String operation) {
      return size(size, operation, false);
    }

    public Builder size(int size, boolean replace) {
      return size(size, "SET", replace);
    }

    public Builder size(int size, String operation, boolean replace) {

      switch (operation) {
        case "SET":
          this.size = replace || this.size == null ? size : Math.max(this.size, size);
        case "ADD":
          this.sizeMod = replace ? size : this.sizeMod + size;
        case "REMOVE":
          this.sizeMod = replace ? -size : this.sizeMod - size;
      }
      return this;
    }

    public Builder useNativeGui(boolean useNativeGui) {
      return useNativeGui(useNativeGui, false);
    }

    public Builder useNativeGui(boolean useNativeGui, boolean replace) {
      this.useNativeGui =
          replace || this.useNativeGui == null ? useNativeGui : this.useNativeGui && useNativeGui;
      return this;
    }

    public Builder renderToggle(boolean renderToggle) {
      return renderToggle(renderToggle, false);
    }

    public Builder renderToggle(boolean renderToggle, boolean replace) {
      this.renderToggle =
          replace || this.renderToggle == null ? renderToggle : this.renderToggle && renderToggle;
      return this;
    }

    public Builder hasCosmetic(boolean hasCosmetic) {
      return hasCosmetic(hasCosmetic, false);
    }

    public Builder hasCosmetic(boolean hasCosmetic, boolean replace) {
      this.hasCosmetic =
          replace || this.hasCosmetic == null ? hasCosmetic : this.hasCosmetic || hasCosmetic;
      return this;
    }

    public Builder dropRule(ICurio.DropRule dropRule) {
      this.dropRule = dropRule;
      return this;
    }

    public Builder dropRule(String dropRule) {
      ICurio.DropRule newRule = EnumUtils.getEnum(ICurio.DropRule.class, dropRule);

      if (newRule == null) {
        Curios.LOGGER.error(dropRule + " is not a valid drop rule!");
      } else {
        this.dropRule = newRule;
      }
      return this;
    }

    public SlotType build() {

      if (this.order == null) {
        this.order = 1000;
      }

      if (this.size == null) {
        this.size = 1;
      }
      this.size += this.sizeMod;
      this.size = Math.max(this.size, 0);

      if (this.useNativeGui == null) {
        this.useNativeGui = true;
      }

      if (this.hasCosmetic == null) {
        this.hasCosmetic = false;
      }

      if (this.renderToggle == null) {
        this.renderToggle = true;
      }
      return new SlotType(this);
    }
  }
}
