/*
 * Copyright (c) 2018-2024 C4
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
 *
 */

package top.theillusivec4.curios.common.slottype;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.EnumUtils;
import top.theillusivec4.curios.CuriosConstants;
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
  private final Set<ResourceLocation> validators;

  public static ISlotType from(CompoundTag tag) {
    Builder builder = new Builder(tag.getString("Identifier"));
    builder.icon(ResourceLocation.parse(tag.getString("Icon")));
    builder.order(tag.getInt("Order"));
    builder.size(tag.getInt("Size"));
    builder.useNativeGui(tag.getBoolean("UseNativeGui"));
    builder.hasCosmetic(tag.getBoolean("HasCosmetic"));
    builder.renderToggle(tag.getBoolean("ToggleRender"));
    builder.dropRule(ICurio.DropRule.values()[tag.getInt("DropRule")]);
    ListTag list = tag.getList("Validators", Tag.TAG_STRING);
    for (Tag tag1 : list) {

      if (tag1 instanceof StringTag stringTag) {
        builder.validator(ResourceLocation.parse(stringTag.getAsString()));
      }
    }
    return builder.build();
  }

  private SlotType(Builder builder) {
    this.identifier = builder.identifier;
    this.order = builder.order;
    this.size = builder.size;
    this.useNativeGui = builder.useNativeGui;
    this.hasCosmetic = builder.hasCosmetic;
    this.icon = builder.icon;
    this.dropRule = builder.dropRule;
    this.renderToggle = builder.renderToggle;
    this.validators = builder.validators;
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
  public Set<ResourceLocation> getValidators() {
    return this.validators;
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

  @Override
  public CompoundTag writeNbt() {
    CompoundTag tag = new CompoundTag();
    tag.putString("Identifier", this.identifier);
    tag.putString("Icon", this.icon.toString());
    tag.putInt("Order", this.order);
    tag.putInt("Size", this.size);
    tag.putBoolean("UseNativeGui", this.useNativeGui);
    tag.putBoolean("HasCosmetic", this.hasCosmetic);
    tag.putBoolean("ToggleRender", this.renderToggle);
    tag.putInt("DropRule", this.dropRule.ordinal());
    ListTag list = new ListTag();

    for (ResourceLocation slotResultPredicate : this.validators) {
      list.add(StringTag.valueOf(slotResultPredicate.toString()));
    }
    tag.put("Validators", list);
    return tag;
  }

  public static class Builder {

    private final String identifier;
    private Integer order = null;
    private Integer size = null;
    private int sizeMod = 0;
    private Boolean useNativeGui = null;
    private Boolean hasCosmetic = null;
    private Boolean renderToggle = null;
    private ResourceLocation icon =
        ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "slot/empty_curio_slot");
    private ICurio.DropRule dropRule = ICurio.DropRule.DEFAULT;
    private Set<ResourceLocation> validators = null;

    public Builder(String identifier) {
      this.identifier = identifier;
    }

    public void apply(Builder builder) {

      if (!builder.identifier.equals(this.identifier)) {
        CuriosConstants.LOG.error("Mismatched slot builders {} and {}", builder.identifier,
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

      if (builder.validators != null) {
        this.validators = Set.copyOf(builder.validators);
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
        case "SET" -> {
          this.size = replace || this.size == null ? size : Math.max(this.size, size);

          if (replace) {
            this.sizeMod = 0;
          }
        }
        case "ADD" -> this.sizeMod = replace ? size : this.sizeMod + size;
        case "REMOVE" -> this.sizeMod = replace ? -size : this.sizeMod - size;
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
        CuriosConstants.LOG.error(dropRule + " is not a valid drop rule!");
      } else {
        this.dropRule = newRule;
      }
      return this;
    }

    public Builder validator(ResourceLocation slotResultPredicate) {

      if (this.validators == null) {
        this.validators = new HashSet<>();
      }
      this.validators.add(slotResultPredicate);
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

      if (this.validators == null) {
        this.validators =
            Set.of(ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "tag"));
      }
      return new SlotType(this);
    }
  }
}
