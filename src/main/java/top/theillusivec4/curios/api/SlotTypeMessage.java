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

package top.theillusivec4.curios.api;

import net.minecraft.resources.ResourceLocation;

/**
 * @deprecated Use the datapack-based approach to slot registration instead of IMC
 * @see <a href="https://docs.illusivesoulworks.com/category/curios">Curios Documentation</a>
 */
@Deprecated
public final class SlotTypeMessage {

  public static final String REGISTER_TYPE = "register_type";
  public static final String MODIFY_TYPE = "modify_type";

  private final String identifier;
  private final Integer priority;
  private final int size;
  private final boolean visible;
  private final boolean cosmetic;
  private final ResourceLocation icon;

  private SlotTypeMessage(Builder builder) {
    this.identifier = builder.identifier;
    this.priority = builder.priority;
    this.size = builder.size;
    this.visible = builder.visible;
    this.cosmetic = builder.cosmetic;
    this.icon = builder.icon;
  }

  @Deprecated
  public String getIdentifier() {
    return this.identifier;
  }

  @Deprecated
  public ResourceLocation getIcon() {
    return this.icon;
  }

  @Deprecated
  public Integer getPriority() {
    return this.priority;
  }

  @Deprecated
  public int getSize() {
    return this.size;
  }

  /**
   * @deprecated Check if {@link SlotTypeMessage#getSize()} returns 0 instead
   */
  @Deprecated
  public boolean isLocked() {
    return getSize() == 0;
  }

  @Deprecated
  public boolean isVisible() {
    return this.visible;
  }

  @Deprecated
  public boolean hasCosmetic() {
    return this.cosmetic;
  }

  @Deprecated
  public static class Builder {

    private final String identifier;
    private Integer priority;
    private int size = 1;
    private boolean visible = true;
    private boolean cosmetic = false;
    private ResourceLocation icon = null;

    @Deprecated
    public Builder(String identifier) {
      this.identifier = identifier;
    }

    @Deprecated
    public Builder icon(ResourceLocation icon) {
      this.icon = icon;
      return this;
    }

    @Deprecated
    public Builder priority(int priority) {
      this.priority = priority;
      return this;
    }

    @Deprecated
    public Builder size(int size) {
      this.size = size;
      return this;
    }

    /**
     * @deprecated Lock slots by setting size to 0 using {@link SlotTypeMessage.Builder#size(int)}
     */
    @Deprecated
    public Builder lock() {
      this.size = 0;
      return this;
    }

    @Deprecated
    public Builder hide() {
      this.visible = false;
      return this;
    }

    @Deprecated
    public Builder cosmetic() {
      this.cosmetic = true;
      return this;
    }

    @Deprecated
    public SlotTypeMessage build() {
      return new SlotTypeMessage(this);
    }
  }
}
